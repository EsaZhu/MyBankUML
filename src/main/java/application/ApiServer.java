package application;

import com.mongodb.client.model.Filters;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import database.Database;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.Map.Entry;

/**
 * Lightweight HTTP API that exposes a minimal set of endpoints backed by the Mongo Database class.
 *
 * Endpoints:
 *  - POST /api/login           -> authenticate by username or ID, returns role + id
 *  - GET  /api/accounts        -> list/filter accounts (accountId, branch, name, type)
 *  - GET  /api/transactions    -> list transactions (optional accountId to filter)
 *  - GET  /api/health          -> simple health check
 */
public class ApiServer {
    private final Database db;

    public ApiServer() {
        this.db = Database.getInstance();
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/health", new HealthHandler(db));
        server.createContext("/api/login", new LoginHandler(db));
        server.createContext("/api/accounts", new AccountsHandler(db));
        server.createContext("/api/transactions", new TransactionsHandler(db));
        server.createContext("/api/customers", new CustomersHandler(db));
        server.createContext("/api/tellers", new TellersHandler(db));
        server.createContext("/api/transactions/transfer", new TransferHandler(db));
        server.createContext("/api/accounts/manage", new AccountMaintenanceHandler(db));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("API server listening on http://localhost:" + port);
    }

    // ===================== Helpers =====================

    private static void addCors(Headers headers) {
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        headers.add("Access-Control-Max-Age", "86400");
    }

    private static void handleOptions(HttpExchange exchange) throws IOException {
        addCors(exchange.getResponseHeaders());
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        addCors(exchange.getResponseHeaders());
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] params = query.split("&");
        for (String param : params) {
            String[] pair = param.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            map.put(key, value);
        }
        return map;
    }

    // ===================== Handlers =====================

    private static class HealthHandler implements HttpHandler {
        private final Database db;

        HealthHandler(Database db) {
            this.db = db;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            boolean ok = db.connect();
            String body = ok ? "{\"status\":\"ok\"}" : "{\"status\":\"down\"}";
            sendJson(exchange, ok ? 200 : 500, body);
        }
    }

    private static class LoginHandler implements HttpHandler {
        private final Database db;

        LoginHandler(Database db) {
            this.db = db;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Document payload = Document.parse(body);
                String username = payload.getString("username");
                String password = payload.getString("password");

                if (username == null || password == null) {
                    sendJson(exchange, 400, "{\"error\":\"Username and password are required\"}");
                    return;
                }

                Document user = db.findUserByUsername(username);
                if (user == null) {
                    // Fallback: allow ID-based login if username is not stored.
                    user = db.getAccountCollection().find(Filters.eq("userID", username)).first();
                    if (user == null) user = db.getTellerCollection().find(Filters.eq("bankTellerID", username)).first();
                    if (user == null) user = db.getAdminCollection().find(Filters.eq("adminID", username)).first();
                }
                if (user == null) {
                    sendJson(exchange, 401, "{\"error\":\"Invalid credentials\"}");
                    return;
                }

                String storedPassword = user.getString("passwordHash");
                if (storedPassword == null) storedPassword = user.getString("password"); // fallback
                if (storedPassword == null || !storedPassword.equals(password)) {
                    sendJson(exchange, 401, "{\"error\":\"Invalid credentials\"}");
                    return;
                }

                String role = resolveRole(user);
                String id = resolveId(user, role);
                String firstName = user.getString("firstName");
                String lastName = user.getString("lastName");
                String displayName = (firstName != null && lastName != null)
                        ? (firstName + " " + lastName)
                        : (user.getString("name") != null ? user.getString("name") : username);

                Document response = new Document()
                        .append("name", displayName)
                        .append("firstName", firstName)
                        .append("lastName", lastName)
                        .append("role", role)
                        .append("id", id);

                sendJson(exchange, 200, response.toJson());
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Login failed\"}");
            }
        }

        private String resolveRole(Document user) {
            if (user.containsKey("bankTellerID")) return "TELLER";
            if (user.containsKey("adminID")) return "ADMIN";
            return "CUSTOMER";
        }

        private String resolveId(Document user, String role) {
            if ("TELLER".equals(role)) return user.getString("bankTellerID");
            if ("ADMIN".equals(role)) return user.getString("adminID");
            return user.getString("userID");
        }
    }

    private static class AccountsHandler implements HttpHandler {
        private final Database db;

        AccountsHandler(Database db) {
            this.db = db;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                String accountId = params.get("accountId");
                String branch = params.get("branch");
                String name = params.get("name");
                String type = params.get("type");

                List<Document> accountDocs = new ArrayList<>();
                db.getAccountCollection().find().into(accountDocs);

                List<Document> expanded = new ArrayList<>();
                for (Document doc : accountDocs) {
                    expanded.addAll(expandAccountDocuments(doc));
                }

                List<Document> filtered = expanded.stream()
                        .filter(doc -> matches(doc, accountId, branch, name, type))
                        .collect(Collectors.toList());

                String json = filtered.stream().map(Document::toJson).collect(Collectors.joining(",", "[", "]"));
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Failed to fetch accounts\"}");
            }
        }

        // Expand a user document that contains an "accounts" array (Savings/Checking/Card) into separate rows
        private List<Document> expandAccountDocuments(Document doc) {
            List<Document> results = new ArrayList<>();
            String baseId = doc.getString("userID");
            if (baseId == null) baseId = doc.getString("accountID");
            String name = doc.getString("name");
            if (name == null && doc.getString("firstName") != null && doc.getString("lastName") != null) {
                name = doc.getString("firstName") + " " + doc.getString("lastName");
            }
            String branch = doc.getString("branch");

            List<Document> subAccounts = doc.getList("accounts", Document.class);
            int accountCounter = 1;
            if (subAccounts != null && !subAccounts.isEmpty()) {
                for (Document sub : subAccounts) {
                    for (Entry<String, Object> entry : sub.entrySet()) {
                        String type = entry.getKey();
                        Double balance = 0.0;
                        Object val = entry.getValue();
                        if (val instanceof Number) {
                            balance = ((Number) val).doubleValue();
                        }
                        Document row = new Document()
                                .append("id", baseId + "-" + type) // unique per sub-account
                                .append("customerId", baseId)
                                .append("customerName", name)
                                .append("branch", branch)
                                .append("type", type)
                                .append("balance", balance);
                        results.add(row);
                    }
                }
            } else {
                String type = doc.getString("accountType");
                if (type == null) type = doc.getString("type");
                Double balance = doc.getDouble("balance");
                Document row = new Document()
                        .append("id", baseId + "-" + (type != null ? type : "Checking"))
                        .append("customerId", baseId)
                        .append("customerName", name)
                        .append("branch", branch)
                        .append("type", type != null ? type : "Checking")
                        .append("balance", balance != null ? balance : 0.0);
                results.add(row);
            }
            return results;
        }

        private boolean matches(Document doc, String accountId, String branch, String name, String type) {
            if (accountId != null && !accountId.isBlank()) {
                String id = doc.getString("id");
                String customerId = doc.getString("customerId");
                boolean idMatch = id != null && id.toLowerCase().contains(accountId.toLowerCase());
                boolean custMatch = customerId != null && customerId.toLowerCase().contains(accountId.toLowerCase());
                if (!idMatch && !custMatch) return false;
            }
            if (branch != null && !branch.isBlank()) {
                String b = doc.getString("branch");
                if (b == null || !b.toLowerCase().contains(branch.toLowerCase())) return false;
            }
            if (name != null && !name.isBlank()) {
                String n = doc.getString("customerName");
                if (n == null || !n.toLowerCase().contains(name.toLowerCase())) return false;
            }
            if (type != null && !type.isBlank()) {
                String t = doc.getString("type");
                if (t == null || !t.equalsIgnoreCase(type)) return false;
            }
            return true;
        }
    }

    private static class TransactionsHandler implements HttpHandler {
        private final Database db;

        TransactionsHandler(Database db) {
            this.db = db;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                String accountId = params.get("accountId");

                List<Document> transactions = new ArrayList<>();
                if (accountId != null && !accountId.isBlank()) {
                    db.getTransactionCollection().find(
                            Filters.or(
                                    Filters.eq("sourceAccountID", accountId),
                                    Filters.eq("receiverAccountID", accountId)
                            )
                    ).into(transactions);
                } else {
                    db.getTransactionCollection().find().into(transactions);
                }

                List<Document> projected = transactions.stream()
                        .map(this::projectTransaction)
                        .collect(Collectors.toList());

                String json = projected.stream().map(Document::toJson).collect(Collectors.joining(",", "[", "]"));
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Failed to fetch transactions\"}");
            }
        }

        private Document projectTransaction(Document doc) {
            String id = doc.getString("transactionID");
            Double amount = doc.getDouble("amount");
            return new Document()
                    .append("id", id)
                    .append("date", doc.getString("transactionDateTime"))
                    .append("type", doc.getString("transactionType"))
                    .append("amount", amount != null ? amount : 0.0)
                    .append("account", doc.getString("sourceAccountID"))
                    .append("receiverAccountID", doc.getString("receiverAccountID"))
                    .append("status", doc.getString("status"));
        }
    }

    // ===================== Mutating Handlers =====================

    private static class CustomersHandler implements HttpHandler {
        private final Database db;

        CustomersHandler(Database db) {
            this.db = db;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) {
                handleCreateCustomer(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDeleteCustomer(exchange);
            } else {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }

        private void handleCreateCustomer(HttpExchange exchange) throws IOException {
            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Document payload = Document.parse(body);
                String userId = payload.getString("userID");
                String username = payload.getString("username");
                String password = payload.getString("password");
                String firstName = payload.getString("firstName");
                String lastName = payload.getString("lastName");
                String branch = payload.getString("branch");
                String accountType = payload.getString("accountType");
                Double initialDeposit = payload.getDouble("initialDeposit");
                if (initialDeposit == null) initialDeposit = 0.0;

                if (userId == null || username == null || password == null) {
                    sendJson(exchange, 400, "{\"error\":\"userID, username, and password are required\"}");
                    return;
                }

                Document existing = db.retrieveAccount(userId);
                if (existing != null) {
                    sendJson(exchange, 409, "{\"error\":\"Customer already exists\"}");
                    return;
                }

                Document newUser = new Document()
                        .append("userID", userId)
                        .append("username", username)
                        .append("passwordHash", password)
                        .append("firstName", firstName)
                        .append("lastName", lastName)
                        .append("branch", branch)
                        .append("accountType", accountType != null ? accountType : "Checking")
                        .append("balance", initialDeposit)
                        .append("transactionHistory", new ArrayList<String>());

                db.addAccount(newUser);
                if (branch != null) {
                    db.getBranchCollection().updateOne(Filters.eq("branchID", branch),
                            new Document("$addToSet", new Document("accounts", userId)));
                }

                sendJson(exchange, 201, "{\"status\":\"created\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Failed to create customer\"}");
            }
        }

        private void handleDeleteCustomer(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath(); // /api/customers/{userId}
                String[] parts = path.split("/");
                if (parts.length < 4) {
                    sendJson(exchange, 400, "{\"error\":\"Missing userID\"}");
                    return;
                }
                String userId = parts[3];

                Document existing = db.retrieveAccount(userId);
                if (existing == null) {
                    sendJson(exchange, 404, "{\"error\":\"Customer not found\"}");
                    return;
                }

                String branch = existing.getString("branch");
                db.removeAccount(userId);
                if (branch != null) {
                    db.getBranchCollection().updateOne(Filters.eq("branchID", branch),
                            new Document("$pull", new Document("accounts", userId)));
                }

                sendJson(exchange, 200, "{\"status\":\"deleted\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Failed to delete customer\"}");
            }
        }
    }

    private static class TellersHandler implements HttpHandler {
        private final Database db;

        TellersHandler(Database db) {
            this.db = db;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }

            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) {
                handleCreateTeller(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDeleteTeller(exchange);
            } else {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }

        private void handleCreateTeller(HttpExchange exchange) throws IOException {
            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Document payload = Document.parse(body);
                String tellerId = payload.getString("bankTellerID");
                String username = payload.getString("username");
                String password = payload.getString("password");
                String branch = payload.getString("branchID");
                String firstName = payload.getString("firstName");
                String lastName = payload.getString("lastName");

                if (tellerId == null || username == null || password == null) {
                    sendJson(exchange, 400, "{\"error\":\"bankTellerID, username, and password are required\"}");
                    return;
                }

                Document existing = db.retrieveTeller(tellerId);
                if (existing != null) {
                    sendJson(exchange, 409, "{\"error\":\"Teller already exists\"}");
                    return;
                }

                Document teller = new Document()
                        .append("bankTellerID", tellerId)
                        .append("username", username)
                        .append("passwordHash", password)
                        .append("branchID", branch)
                        .append("firstName", firstName)
                        .append("lastName", lastName);

                db.addTeller(teller);
                sendJson(exchange, 201, "{\"status\":\"created\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Failed to create teller\"}");
            }
        }

        private void handleDeleteTeller(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath(); // /api/tellers/{tellerId}
                String[] parts = path.split("/");
                if (parts.length < 4) {
                    sendJson(exchange, 400, "{\"error\":\"Missing tellerID\"}");
                    return;
                }
                String tellerId = parts[3];

                Document existing = db.retrieveTeller(tellerId);
                if (existing == null) {
                    sendJson(exchange, 404, "{\"error\":\"Teller not found\"}");
                    return;
                }

                db.removeTeller(tellerId);
                sendJson(exchange, 200, "{\"status\":\"deleted\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Failed to delete teller\"}");
            }
        }
    }

    private static class TransferHandler implements HttpHandler {
        private final Database db;

        TransferHandler(Database db) {
            this.db = db;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Document payload = Document.parse(body);
                String from = payload.getString("fromAccount");
                String to = payload.getString("toAccount");
                Number amountNum = payload.get("amount", Number.class);
                double amount = amountNum != null ? amountNum.doubleValue() : 0.0;
                String type = payload.getString("type"); // deposit | withdraw | transfer
                if (type == null || amount <= 0) {
                    sendJson(exchange, 400, "{\"error\":\"type and positive amount are required\"}");
                    return;
                }

                // Parse sub-account identifiers (e.g., U001-Savings)
                String fromId = null, fromSub = null;
                if (from != null && !from.isBlank()) {
                    String[] parts = from.split("-", 2);
                    fromId = parts[0];
                    fromSub = parts.length > 1 ? parts[1] : null;
                }
                String toId = null, toSub = null;
                if (to != null && !to.isBlank()) {
                    String[] parts = to.split("-", 2);
                    toId = parts[0];
                    toSub = parts.length > 1 ? parts[1] : null;
                }

                // Fetch account documents directly
                Document fromDoc = (fromId != null && !fromId.isBlank())
                        ? db.getAccountCollection().find(Filters.eq("userID", fromId)).first()
                        : null;
                Document toDoc = (toId != null && !toId.isBlank())
                        ? db.getAccountCollection().find(Filters.eq("userID", toId)).first()
                        : null;

                if ("withdraw".equalsIgnoreCase(type) || "transfer".equalsIgnoreCase(type)) {
                    if (fromDoc == null) {
                        sendJson(exchange, 404, "{\"error\":\"Source account not found\"}");
                        return;
                    }
                    double fromBal = getSubAccountBalance(fromDoc, fromSub);
                    if (fromBal < amount) {
                        sendJson(exchange, 400, "{\"error\":\"Insufficient funds\"}");
                        return;
                    }
                    updateSubAccountBalance(fromDoc, fromId, fromSub, fromBal - amount);
                }

                if ("deposit".equalsIgnoreCase(type) || "transfer".equalsIgnoreCase(type)) {
                    if (toDoc == null) {
                        sendJson(exchange, 404, "{\"error\":\"Destination account not found\"}");
                        return;
                    }
                    double toBal = getSubAccountBalance(toDoc, toSub);
                    updateSubAccountBalance(toDoc, toId, toSub, toBal + amount);
                }

                String transactionId = payload.getString("transactionID");
                if (transactionId == null) {
                    transactionId = "TX-" + System.currentTimeMillis();
                }
                Document tx = new Document()
                        .append("transactionID", transactionId)
                        .append("sourceAccountID", from)
                        .append("receiverAccountID", to)
                        .append("amount", amount)
                        .append("transactionType", type)
                        .append("transactionDateTime", payload.getString("transactionDateTime"))
                        .append("status", "COMPLETED");
                db.addTransaction(tx);

                sendJson(exchange, 200, tx.toJson());
            } catch (Exception e) {
                e.printStackTrace();
                    sendJson(exchange, 500, "{\"error\":\"Failed to process transaction\"}");
                }
            }

            private double getSubAccountBalance(Document doc, String subType) {
                if (subType == null) {
                    Number bal = doc.get("balance", Number.class);
                    return bal != null ? bal.doubleValue() : 0.0;
                }
                List<Document> subs = doc.getList("accounts", Document.class);
                if (subs != null) {
                    for (Document sub : subs) {
                        if (sub.containsKey(subType)) {
                            Object val = sub.get(subType);
                            if (val instanceof Number) {
                                return ((Number) val).doubleValue();
                            }
                        }
                    }
                }
                return 0.0;
            }

            private void updateSubAccountBalance(Document doc, String baseId, String subType, double newBalance) {
                if (subType == null) {
                    db.updateAccount(baseId, new Document("balance", newBalance));
                    return;
                }
                List<Document> subs = doc.getList("accounts", Document.class);
                if (subs == null) subs = new ArrayList<>();
                boolean updated = false;
                for (Document sub : subs) {
                    if (sub.containsKey(subType)) {
                        sub.put(subType, newBalance);
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    Document newSub = new Document().append(subType, newBalance);
                    subs.add(newSub);
                }
                db.getAccountCollection().updateOne(Filters.eq("userID", baseId),
                        new Document("$set", new Document("accounts", subs)));
            }
        }

    private static class AccountMaintenanceHandler implements HttpHandler {
        private final Database db;

        AccountMaintenanceHandler(Database db) {
            this.db = db;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleOptions(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Document payload = Document.parse(body);
                String action = payload.getString("action");
                String userId = payload.getString("userID");
                String accountType = payload.getString("accountType");

                if (action == null || userId == null) {
                    sendJson(exchange, 400, "{\"error\":\"action and userID are required\"}");
                    return;
                }

                Document existing = db.retrieveAccount(userId);
                if (existing == null) {
                    sendJson(exchange, 404, "{\"error\":\"Customer not found\"}");
                    return;
                }

                if ("close".equalsIgnoreCase(action)) {
                    db.removeAccount(userId);
                    String branch = existing.getString("branch");
                    if (branch != null) {
                        db.getBranchCollection().updateOne(Filters.eq("branchID", branch),
                                new Document("$pull", new Document("accounts", userId)));
                    }
                    sendJson(exchange, 200, "{\"status\":\"account closed\"}");
                    return;
                }

                // open/update
                Document updates = new Document();
                if (accountType != null) {
                    updates.append("accountType", accountType);
                }
                if (!updates.isEmpty()) {
                    db.updateAccount(userId, updates);
                }
                sendJson(exchange, 200, "{\"status\":\"account updated\"}");
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Failed to manage account\"}");
            }
        }
    }
}
