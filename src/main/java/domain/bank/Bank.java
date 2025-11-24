package domain.bank;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import database.Database;
import domain.users.IUser;
import domain.users.UserAccount;

// removed lombok usage to avoid external dependency
public class Bank {
    // placeholder per UML; kept minimal to compile
    public String name;
    public String bankID;
    public ArrayList<Branch> branches;
    public String searchID;
    public ArrayList<String> criteriaList;
    public ArrayList<IUser> resultList;
    public Database database;

    public Bank(String name, String bankID, ArrayList<Branch> branches, String searchID, ArrayList<String> criteriaList, ArrayList<IUser> resultList, Database database) {
        this.name = name;
        this.bankID = bankID;
        this.branches = branches;
        this.searchID = searchID;
        this.criteriaList = criteriaList;
        this.resultList = resultList;

        this.database = database;
        database.connect();
    }

    public void addBranch(Branch branch) {
        Document branchDoc = database.branchToDocument(branch);
        database.addBranch(branchDoc);
    }

    public void printBankInfo() {

    }

    public IUser searchByID(String id) {
        IUser user = database.findUserByID(id);
        this.resultList = new ArrayList<>();
        this.resultList.add(user);
        return user;
    }

    public UserAccount searchByUsername(String username) {
        database.findUserByUsername(username);
        return null;
    }

    public ArrayList<IUser> searchByAttribute(String key, String value) {
        ArrayList<IUser> users = database.searchAccountsByAttribute(key, value);
        this.resultList = users;
        return users;
    }

    public ArrayList<UserAccount> filterResults(List<String> filters) {
        return null;
    }

    public void displayResults() {

    }
}
