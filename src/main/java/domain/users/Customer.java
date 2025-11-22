package domain.users;

public class Customer {
    private final String name;

    public Customer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void printCustomerInfo() {
        System.out.println("Customer name: " + name);
    }
}
