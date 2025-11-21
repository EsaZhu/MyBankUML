package main.domain.accounts;

import main.domain.users.Account;
import main.domain.users.Customer;

public class Checking extends Account {
    public Checking(Customer customer) {
        super(customer);
    }

    @Override
    public void pay() {
        System.out.println("Check payment for customer: " + customer.getName());
    }

    @Override
    public void receipt() {
        System.out.println("Check receipt for customer: " + customer.getName());
    }
}

