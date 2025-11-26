import React, { useMemo } from "react";
import { useLocation, useParams, Link } from "react-router-dom";
import TransferForm from "../components/TransferForm.jsx";
import TransactionsTable from "../components/TransactionsTable.jsx";

export default function TellerCustomerProfile({ accounts = [], transactions = [] }) {
  const { accountId } = useParams();
  const location = useLocation();
  const accountFromState = location.state?.account;

  const account = useMemo(() => {
    if (accountFromState) return accountFromState;
    return accounts.find((a) => a.id === accountId);
  }, [accountFromState, accounts, accountId]);

  const accountTransactions = useMemo(() => {
    if (!transactions) return [];
    return transactions.filter(
      (tx) =>
        tx.account === account?.id ||
        tx.account === accountId ||
        tx.receiverAccountID === account?.id ||
        tx.receiverAccountID === accountId
    );
  }, [transactions, account, accountId]);

  return (
    <section>
      <h2>Customer Profile</h2>
      <p className="muted">
        Details for account {accountId}. This view should be driven by the database lookup
        based on the account or customer id.
      </p>

      <div className="card">
         <h3>Customer</h3>
        <p>
          <strong>Name:</strong> {account?.customerName || "—"}
        </p>
        <h3>Account</h3>
        <p>
          <strong>Account ID:</strong> {account?.id || accountId}
        </p>
        <p>
          <strong>Type:</strong> {account?.type || "—"}
        </p>
        <p>
          <strong>Branch:</strong> {account?.branch || "—"}
        </p>
        <p>
          <strong>Balance:</strong> {account?.balance ? `$${account.balance.toLocaleString()}` : "—"}
        </p>
      </div>

      <div className="card" style={{ marginTop: "12px" }}>    
          <>
            <h3>Record a Transaction</h3>
            <TransferForm accounts={accounts} forTeller />
            <h3 style={{ marginTop: "16px" }}>Recent Branch Transactions</h3>
            <TransactionsTable transactions={accountTransactions} />
          </>
      
      </div>

      <p className="muted" style={{ marginTop: "10px" }}>
        Replace this placeholder with data returned by the backend teller customer details
        endpoint.
      </p>

      <Link className="link-btn" to="/teller" style={{ marginTop: "12px", display: "inline-block" }}>
        Back to teller dashboard
      </Link>
    </section>
  );
}
