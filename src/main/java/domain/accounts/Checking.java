package domain.accounts;

import domain.users.UserAccount;

public class Checking extends UserAccount {

    private double overdraftLimit; // maximum allowed negative balance
    private double minBalance; // enforced minimum balance on the account
    private double monthlyFee; // monthly maintenance fee

    public Checking(String userID, String name, String email, String passwordHash, double balance, domain.bank.Branch branch,
            double overdraftLimit, double minBalance, double monthlyFee) {
        super(userID, name, email, passwordHash, balance, branch);
        this.overdraftLimit = overdraftLimit;
        this.minBalance = minBalance;
        this.monthlyFee = monthlyFee;
    }

    /**
     * Overridden withdraw method to enforce overdraft limit
     * 
     * @param amount
     */
    @Override
    public void withdraw(double amount) {
        // invalid if amount is negative or if withdrawal exceeds overdraft limit
        if (amount <= 0 || super.getBalance() - amount < -this.overdraftLimit
                || super.getBalance() - amount < this.minBalance) {
            return;
        }
        super.withdraw(amount);
    }

    /**
     * Simple method to apply monthly fee
     */
    public void applyMonthlyFee() {
        this.withdraw(monthlyFee);
    }

    /**
     * GUI display method to check minimum balance
     */
    public void checkMinBalance() {
        // TO BE REPLACED BY GUI
    }

}
