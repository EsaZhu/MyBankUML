package domain.bank;

import java.util.ArrayList;
import java.util.List;
import domain.users.UserAccount;

public class Branch {

    private String branchID;
    private String branchName;
    private String address;
    private List<UserAccount> accounts;

    public Branch(String branchID, String branchName, String address) {
        this.branchID = branchID;
        this.branchName = branchName;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    public void addAccount(UserAccount a) {
        accounts.add(a);
    }

    public void removeAccount(String id) {
        accounts.removeIf(acc -> acc.getUserID().equals(id));
    }

    public UserAccount getAccount(String id) {
        for (UserAccount acc : accounts) {
            if (acc.getUserID().equals(id)) {
                return acc;
            }
        }
        return null;
    }

    public void printBranchInfo() {
        System.out.println("Branch: " + branchName);
        System.out.println("ID: " + branchID);
        System.out.println("Address: " + address);
        System.out.println("Accounts: " + accounts.size());
    }
}
