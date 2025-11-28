package domain.accounts;

import domain.bank.Branch;
import domain.enums.TransactionStatus;
import domain.transactions.Transaction;
import domain.users.Account;
import domain.users.UserAccount;

public class Savings extends Account {

    private double interestRate;
    private double minimumBalance;

    public Savings(String userID, String accountID, double balance, double interestRate,
            double minimumBalance) {
        super(userID, accountID, "SAV", balance);
        this.interestRate = interestRate;
        this.minimumBalance = minimumBalance;
    }

    /**
     * Overridden withdraw method to enforce minimum balance
     * 
     * @param amount
     */
    @Override
    public void withdraw(double amount) {
        if (amount <= 0 || super.getBalance() - amount < this.minimumBalance) {
            return;
        }
        Transaction withdrawTransaction = new Transaction(
                "TXN_" + super.accountHeader + "_W" + System.currentTimeMillis(),
                super.getUserID(),
                super.getAccountID(),
                null,
                null,
                amount,
                "WITHDRAW",
                java.time.LocalDateTime.now(),
                TransactionStatus.PENDING);
        withdrawTransaction.execute();
    }

    /**
     * Method to calculate and deposit interest
     * 
     * @return
     */
    public double calculateInterest() {
        Transaction interestTransaction = new Transaction(
                "TXN_" + super.accountHeader + "_I" + System.currentTimeMillis(),
                super.getUserID(),
                super.getAccountID(),
                null,
                null,
                super.getBalance() * interestRate,
                "DEPOSIT",
                java.time.LocalDateTime.now(),
                TransactionStatus.PENDING);
        interestTransaction.execute();
        return super.getBalance() * interestRate;
    }
    
    /*-------------------- Getters and Setters --------------------*/
    public double receipt() {
        return super.getBalance() * interestRate;
    }
}
