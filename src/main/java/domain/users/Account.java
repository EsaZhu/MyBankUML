package domain.users;

import domain.transactions.Transaction;

import java.util.ArrayList;
import java.util.List;

public abstract class Account {
    protected Customer customer;
    protected List<Transaction> transactions;

    public Account(Customer customer) {
        this.customer = customer;
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public abstract void pay();
    public abstract void receipt();
}

