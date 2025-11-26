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

                List<Document> accounts = db.getAllAccounts();
                List<Document> filtered = accounts.stream()
                        .map(this::projectAccount)
                        .filter(doc -> matches(doc, accountId, branch, name, type))
                        .collect(Collectors.toList());

                String json = filtered.stream().map(Document::toJson).collect(Collectors.joining(",", "[", "]"));
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"error\":\"Failed to fetch accounts\"}");
            }
        }

        private Document projectAccount(Document doc) {
            String id = doc.getString("userID");
            if (id == null) id = doc.getString("accountID");
            String name = doc.getString("name");
            String branch = doc.getString("branch");
            String type = doc.getString("accountType");
            if (type == null) type = doc.getString("type");
            Double balance = doc.getDouble("balance");

            return new Document()
                    .append("id", id)
                    .append("customerName", name)
                    .append("branch", branch)
                    .append("type", type != null ? type : "Checking")
                    .append("balance", balance != null ? balance : 0.0);
        }

        private boolean matches(Document doc, String accountId, String branch, String name, String type) {
            if (accountId != null && !accountId.isBlank()) {
                String id = doc.getString("id");
                if (id == null || !id.toLowerCase().contains(accountId.toLowerCase())) return false;
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
                    transactions.addAll(db.getTransactionHistory(accountId));
                } else {
                    transactions.addAll(db.getAllTransactions());
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
}
