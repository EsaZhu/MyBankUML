package domain.accounts;

import domain.enums.TransactionStatus;
import domain.transactions.Transaction;
import domain.users.Account;
import domain.users.UserAccount;

public class Card extends Account {

    private double cardLimit; // maximum credit limit
    private double interest;
    private double minimumPayment;

    public Card(String userID, String accountID, double balance, double cardLimit, double interest,
            double minimumPayment) {
        super(userID, accountID, "CRD", balance);
        this.cardLimit = cardLimit;
        this.interest = interest;
        this.minimumPayment = minimumPayment;
    }

    /**
     * Overridden deposit method to enforce card limit
     * 
     * @param amount
     */
    @Override
    public void deposit(double amount) {
        // invalid if amount is negative or if deposit exceeds card limit
        if (amount <= 0 || super.getBalance() + amount > this.cardLimit) {
            return;
        }
        Transaction depositTransaction = new Transaction(
                "TXN_" + super.accountHeader + "_D" + System.currentTimeMillis(),
                super.getUserID(),
                super.getAccountID(),
                null,
                null,
                amount,
                "DEPOSIT",
                java.time.LocalDateTime.now(),
                TransactionStatus.PENDING);
        depositTransaction.execute();
        super.deposit(amount);
    }

    /**
     * Method to apply monthly fee based on interest and minimum payment
     */
    public void applyMonthlyFee() {
        this.minimumPayment = (this.cardLimit - super.getBalance()) * interest; // interest on used credit
        this.withdraw(minimumPayment); // deduct new minimum payment from balance
    }


    /*-------------------- Getters and Setters --------------------*/

    public double checkCardLimit() {
        return this.cardLimit;
    }

    public double getMinimumPayment(){
        return this.minimumPayment = (this.cardLimit - super.getBalance()) * interest;
    }
}
