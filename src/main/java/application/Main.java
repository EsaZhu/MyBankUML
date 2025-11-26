package application;

public class Main {
    public static void main(String[] args) {
        try {
            int port = 8080;
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
            ApiServer apiServer = new ApiServer();
            apiServer.start(port);
        } catch (Exception e) {
            System.err.println("✗ Failed to start API server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
