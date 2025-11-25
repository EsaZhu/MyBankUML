package domain.bank;

public class Branch {

    private String branchID;
    private String branchName;
    private String address;

    public Branch(String branchID, String branchName, String address) {
        this.branchID = branchID;
        this.branchName = branchName;
        this.address = address;
    }

    public String getBranchID() {
        return branchID;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getAddress() {
        return address;
    }

    public void printBranchInfo() {
        System.out.println("Branch Name: " + branchName);
        System.out.println("Branch ID: " + branchID);
        System.out.println("Address: " + address);
    }
}
