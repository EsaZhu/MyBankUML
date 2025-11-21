package main.domain.accounts;

import main.domain.users.Account;
import main.domain.users.Customer;

public class Savings extends Account {
    public Savings(Customer customer) {
        super(customer);
    }

    @Override
    public void pay() {
        System.out.println("Payment From saving account For: " + customer.getName());
    }

    @Override
    public void receipt() {
        System.out.println("Payment receipt from saving account for: " + customer.getName());
    }
}
