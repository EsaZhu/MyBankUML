package application;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import database.Database;
import domain.accounts.Card;
import domain.accounts.Checking;
import domain.accounts.Savings;
import domain.transactions.Transaction;
import domain.users.Account;
import domain.users.BankTellerAccount;
import domain.users.DatabaseAdministratorAccount;
import domain.users.IUser;
import domain.users.UserAccount;
import org.bson.Document;

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
import java.time.LocalDateTime;

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
        server.createContext("/api/users", new UsersHandler(db));
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

                IUser user = db.retrieveUserByUsername(username);
                if (user == null) {
                    // fallback: try ids
                    user = db.retrieveUser(username);
                }
                if (user == null || user.getPasswordHash() == null || !user.getPasswordHash().equals(password)) {
                    sendJson(exchange, 401, "{\"error\":\"Invalid credentials\"}");
                    return;
                }

                String role;
                String id;
                String firstName = null;
                String lastName = null;
                if (user instanceof BankTellerAccount) {
                    role = "TELLER";
                    id = ((BankTellerAccount) user).getBankTellerID();
                    firstName = ((BankTellerAccount) user).getFirstName();
                    lastName = ((BankTellerAccount) user).getLastName();
                } else if (user instanceof DatabaseAdministratorAccount) {
                    role = "ADMIN";
                    id = ((DatabaseAdministratorAccount) user).getAdminID();
                    firstName = ((DatabaseAdministratorAccount) user).getFirstname();
                    lastName = ((DatabaseAdministratorAccount) user).getLastname();
                } else {
                    role = "CUSTOMER";
                    UserAccount ua = (UserAccount) user;
                    id = ua.getUserID();
                    firstName = ua.getFirstName();
                    lastName = ua.getLastName();
                }
                String displayName = (firstName != null && lastName != null)
                        ? (firstName + " " + lastName)
                        : username;

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

                List<UserAccount> users = db.getAllAccounts();
                List<Document> expanded = new ArrayList<>();
                for (UserAccount ua : users) {
                    expanded.addAll(expandUserAccount(ua));
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

        private List<Document> expandUserAccount(UserAccount ua) {
            List<Document> results = new ArrayList<>();
            String baseId = ua.getUserID();
            String name = ua.getFirstName() != null && ua.getLastName() != null
                    ? ua.getFirstName() + " " + ua.getLastName()
                    : ua.getUsername();
            String branch = ua.getBranchID();

            List<Account> subAccounts = ua.getAccounts();
            if (subAccounts != null) {
                for (Account acc : subAccounts) {
                    String type = acc.getAccountHeader();
                    String friendlyType = "Checking";
                    if ("SAV".equalsIgnoreCase(type)) friendlyType = "Savings";
                    else if ("CRD".equalsIgnoreCase(type)) friendlyType = "Card";
                    else if ("CHK".equalsIgnoreCase(type)) friendlyType = "Checking";
                    Document row = new Document()
                            .append("id", acc.getAccountID())
                            .append("customerId", baseId)
                            .append("customerName", name)
                            .append("branch", branch)
                            .append("type", friendlyType)
                            .append("balance", acc.getBalance());
                    results.add(row);
                }
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

                List<Transaction> txs;
                if (accountId != null && !accountId.isBlank()) {
                    txs = db.getTransactionHistory(accountId);
                } else {
                    txs = db.getAllTransactions();
                }

                List<Document> projected = txs.stream()
                        .map(this::projectTransaction)
                        .filter(tx -> matchesAccount(tx, accountId))
                        .collect(Collectors.toList());

                String json = projected.stream().map(Document::toJson).collect(Collectors.joining(",", "[", "]"));
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Failed to fetch transactions\"}");
            }
        }

        private Document projectTransaction(Transaction tx) {
            String id = tx.getTransactionID();
            Double amount = tx.getAmount();
            return new Document()
                    .append("id", id)
                    .append("date", tx.getTransactionDateTime() != null ? tx.getTransactionDateTime().toString() : null)
                    .append("type", tx.getTransactionType())
                    .append("amount", amount != null ? amount : 0.0)
                    .append("account", tx.getSourceAccountID())
                    .append("receiverAccountID", tx.getReceiverAccountID())
                    .append("status", tx.getStatus() != null ? tx.getStatus().toString() : null);
        }

        private boolean matchesAccount(Document tx, String accountId) {
            if (accountId == null || accountId.isBlank()) return true;
            String src = tx.getString("sourceAccountID");
            String recv = tx.getString("receiverAccountID");
            return (src != null && src.contains(accountId)) || (recv != null && recv.contains(accountId));
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
                Number initNum = payload.get("initialDeposit", Number.class);
                double initialDeposit = initNum != null ? initNum.doubleValue() : 0.0;

                if (userId == null || username == null || password == null) {
                    sendJson(exchange, 400, "{\"error\":\"userID, username, and password are required\"}");
                    return;
                }

                if (db.retrieveUserAccount(userId) != null) {
                    sendJson(exchange, 409, "{\"error\":\"Customer already exists\"}");
                    return;
                }

                List<Account> accounts = new ArrayList<>();
                String acctType = accountType != null ? accountType : "Checking";
                String acctId = userId + "-" + acctType;
                switch (acctType.toLowerCase()) {
                    case "savings":
                        accounts.add(new Savings(userId, acctId, initialDeposit, 0, 0));
                        break;
                    case "card":
                        accounts.add(new Card(userId, acctId, initialDeposit, 0, 0, 0));
                        break;
                    default:
                        accounts.add(new Checking(userId, acctId, initialDeposit, 0, 0, 0));
                }

                UserAccount newUser = new UserAccount(userId, username, firstName, lastName, password, branch, accounts);
                newUser.setTransactionHistory(new ArrayList<>());
                db.addAccount(newUser);
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

                UserAccount existing = db.retrieveUserAccount(userId);
                if (existing == null) {
                    sendJson(exchange, 404, "{\"error\":\"Customer not found\"}");
                    return;
                }

                db.removeUserAccount(userId);

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

                if (db.retrieveTeller(tellerId) != null) {
                    sendJson(exchange, 409, "{\"error\":\"Teller already exists\"}");
                    return;
                }

                BankTellerAccount teller = new BankTellerAccount(tellerId, username, firstName, lastName, password, branch, db);
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

                IUser existing = db.retrieveTeller(tellerId);
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

                String transactionId = payload.getString("transactionID");
                if (transactionId == null) {
                    transactionId = "TX-" + System.currentTimeMillis();
                }

                String fromUser = extractUserId(from);
                String toUser = extractUserId(to);
                Transaction tx;
                switch (type.toLowerCase()) {
                    case "deposit":
                        tx = new Transaction(transactionId, toUser, to, toUser, to, amount, "DEPOSIT",
                                java.time.LocalDateTime.now(), domain.enums.TransactionStatus.PENDING);
                        break;
                    case "withdraw":
                        tx = new Transaction(transactionId, fromUser, from, fromUser, from, amount, "WITHDRAW",
                                java.time.LocalDateTime.now(), domain.enums.TransactionStatus.PENDING);
                        break;
                    default:
                        tx = new Transaction(transactionId, fromUser, from, toUser, to, amount, "TRANSFER",
                                java.time.LocalDateTime.now(), domain.enums.TransactionStatus.PENDING);
                }

                if (!tx.execute()) {
                    sendJson(exchange, 400, "{\"error\":\"Failed to process transaction\"}");
                    return;
                }

                db.addTransaction(tx);
                sendJson(exchange, 200, transactionToDoc(tx).toJson());
            } catch (Exception e) {
                e.printStackTrace();
                    sendJson(exchange, 500, "{\"error\":\"Failed to process transaction\"}");
                }
            }

        private Document transactionToDoc(Transaction tx) {
            return new Document()
                    .append("transactionID", tx.getTransactionID())
                    .append("sourceAccountID", tx.getSourceAccountID())
                    .append("receiverAccountID", tx.getReceiverAccountID())
                    .append("amount", tx.getAmount())
                    .append("transactionType", tx.getTransactionType())
                    .append("transactionDateTime",
                            tx.getTransactionDateTime() != null ? tx.getTransactionDateTime().toString() : null)
                    .append("status", tx.getStatus() != null ? tx.getStatus().toString() : null);
        }

        private String extractUserId(String accountId) {
            if (accountId == null) return null;
            int idx = accountId.indexOf("-");
            return idx > 0 ? accountId.substring(0, idx) : accountId;
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

                UserAccount existing = db.retrieveUserAccount(userId);
                if (existing == null) {
                    sendJson(exchange, 404, "{\"error\":\"Customer not found\"}");
                    return;
                }

                if ("close".equalsIgnoreCase(action)) {
                    db.removeUserAccount(userId);
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

    private static class UsersHandler implements HttpHandler {
        private final Database db;

        UsersHandler(Database db) {
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
                String path = exchange.getRequestURI().getPath(); // /api/users/{userId}
                String[] parts = path.split("/");
                if (parts.length < 4) {
                    sendJson(exchange, 400, "{\"error\":\"Missing userID\"}");
                    return;
                }
                String userId = parts[3];
                UserAccount user = db.retrieveUserAccount(userId);
                if (user == null) {
                    sendJson(exchange, 404, "{\"error\":\"User not found\"}");
                    return;
                }
                Document doc = new Document()
                        .append("userID", user.getUserID())
                        .append("username", user.getUsername())
                        .append("firstName", user.getFirstName())
                        .append("lastName", user.getLastName())
                        .append("branch", user.getBranchID())
                        .append("transactionHistory", user.getTransactionHistory());
                sendJson(exchange, 200, doc.toJson());
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Failed to fetch user\"}");
            }
        }
    }
}
