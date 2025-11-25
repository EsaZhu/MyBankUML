package domain.accounts;

import domain.users.Account;
import domain.users.Customer;

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
        System.out.println("Receipt from saving account for: " + customer.getName());
    }
}
