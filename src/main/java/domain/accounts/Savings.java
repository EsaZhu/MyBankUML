package domain.accounts;

import domain.users.UserAccount;

public class Savings extends UserAccount {

    private double interestRate;
    private double minimumBalance;

    public Savings(String userID, String name, String email, String passwordHash, double balance, domain.bank.Branch branch,
            double interestRate, double minumumBalance) {
        super(userID, name, email, passwordHash, balance, branch);
        this.interestRate = interestRate;
        this.minimumBalance = minumumBalance;
    }

    /**
     * Overridden withdraw method to enforce minimum balance
     * @param amount
     */
    @Override
    public void withdraw(double amount) {
        if (amount <= 0 || super.getBalance() - amount < this.minimumBalance) {
            return;
        }
        super.withdraw(amount);
    }

    /**
     * Method to calculate and deposit interest
     * @return
     */
    public double calculateInterest() {
        double interest = super.getBalance() * interestRate;
        super.deposit(interest);
        return interest;
    }

    /**
     * GUI display method to show receipt of interest calculation
     */
    public void receipt() {
        // TO BE REPLACED BY GUI
    }
}
