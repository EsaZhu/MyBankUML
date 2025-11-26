package application;

import database.Database;
import domain.enums.UserRole;
import domain.users.IUser;

public class Login {
    private String username;
    private String password;
    private boolean isAuthenticated;
    private IUser authenticatedUser;
    private Database database;

    public Login(Database database) {
        this.database = database;
        this.isAuthenticated = false;
        this.authenticatedUser = null;
    }

    // Attempts to login user based on credentials, returns true if success or false otherwise.
    public boolean login(String username, String password) {
        // storing the credentials locally
        this.username = username;
        this.password = password;
    //fix later
       /* IUser user =  database.retrieveAccount(username);

        if (user != null && user.getPasswordHash().equals(password)) {
            this.authenticatedUser = user;
            this.isAuthenticated = true;
            return true;
        } */

        //login failure case
        this.authenticatedUser = null;
        this.isAuthenticated = false;
        return false;
    }

    public void logout() {
        this.isAuthenticated = false;
        this.authenticatedUser = null;
        this.username = null;
        this.password = null;
    }

    public String getUsername() {
        return this.username;
    }

    public UserRole getRole() {
        if (isAuthenticated && authenticatedUser != null) {
            return authenticatedUser.getRole();
        }

        return UserRole.NONE;
    }

    public void redirectUser() {
        if (!isAuthenticated || authenticatedUser == null) {
            System.out.println("User not authenticated.");
            return;
        }

        UserRole role = authenticatedUser.getRole();

        switch (role) {
            case USER:
                System.out.println("Redirecting to User dashboard...");
                break;
            case TELLER:
                System.out.println("Redirecting to Bank Teller dashboard...");
                break;
            case ADMIN:
                System.out.println("Redirecting to Admin dashboard...");
                break;
            default:
                System.out.println("Unknown role. Cannot redirect.");
        }
    }

}
