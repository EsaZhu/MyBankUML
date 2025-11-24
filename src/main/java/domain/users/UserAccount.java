package domain.users;

public class UserAccount implements IUser {
    private String userID;
    

    public UserAccount(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }
    
}
