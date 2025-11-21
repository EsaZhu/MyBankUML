package main.application;

import main.domain.users.Customer;
import main.domain.accounts.Card;
import main.domain.accounts.Checking;
import main.domain.accounts.Savings;
import main.domain.transactions.Transaction;

public class Main {
    public static void main(String[] args) {
        Customer customer = new Customer("Shayan Aminaei");
        customer.printCustomerInfo();

        Card card = new Card(customer);
        Checking check = new Checking(customer);
        Savings saving = new Savings(customer);

        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();
        Transaction t3 = new Transaction();

        card.addTransaction(t1);
        check.addTransaction(t2);
        saving.addTransaction(t3);

        System.out.println("Card   transactions count:   " + card.getTransactions().size());
        System.out.println("Check  transactions count:   " + check.getTransactions().size());
        System.out.println("Saving transactions count:   " + saving.getTransactions().size());
    }
}
