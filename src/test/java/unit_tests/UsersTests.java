// package unit_tests;

// import org.junit.jupiter.api.Test;

// import domain.transactions.Transaction;
// import domain.users.*;

// public class UsersTests {

//     // ---------------- DATABASE ADMINISTRATOR ----------------

//     @Test
//     static void test_Admin_CreateAccounts() {
//         System.out.println("\n--- Admin.manageTellerAccounts + manageCustomerAccounts ---");

//         DatabaseAdministratorAccount admin = new DatabaseAdministratorAccount("A1", "admin", "F", "L", "hash");

//         admin.manageTellerAccounts("CREATE", "BT1", "pass", "BR1");
//         admin.manageCustomerAccounts("CREATE", "U1", "John", "Doe", "pass", "BR1");

//         var tellers = admin.searchAccounts("teller");
//         var customers = admin.searchAccounts("customer");

//         if (tellers.size() >= 1 && customers.size() >= 1)
//             System.out.println("PASS");
//         else
//             System.out.println("FAIL");
//     }

//     @Test
//     static void test_Admin_ReverseTransaction() {
//         System.out.println("\n--- Admin.reverseTransactions() ---");

//         DatabaseAdministratorAccount admin = new DatabaseAdministratorAccount("A1", "ad", "F", "L", "p");

//         Transaction t = new Transaction(
//                 "TRX", "A1", "A2", 200, "TRANSFER",
//                 java.time.LocalDateTime.now(),
//                 TransactionStatus.COMPLETED);

//         admin.reverseTransactions(t);

//         if (t.getStatus() == TransactionStatus.REVERSED)
//             System.out.println("PASS");
//         else
//             System.out.println("FAIL");
//     }

//     @Test
//     static void test_Admin_EmptyAuditReport() {
//         System.out.println("\n--- Admin.generateReports() empty ---");

//         DatabaseAdministratorAccount admin = new DatabaseAdministratorAccount("A2", "ad2", "F", "L", "p");

//         Report r = admin.generateReports();

//         if (r.isEmpty())
//             System.out.println("PASS");
//         else
//             System.out.println("FAIL");
//     }
// }
