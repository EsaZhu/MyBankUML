import React, { useMemo, useState } from "react";
import { recordTransaction } from "../api";
import SummaryCards from "../components/SummaryCards.jsx";
import AccountsTable from "../components/AccountsTable.jsx";
import TransactionsTable from "../components/TransactionsTable.jsx";
import TransferForm from "../components/TransferForm.jsx";

export default function CustomerDashboard({ user, accounts, transactions, onRefresh }) {
  const [tab, setTab] = useState("overview"); // overview | accounts | transfer
  const displayName =
    user?.firstName && user?.lastName ? `${user.firstName} ${user.lastName}` : user?.name;
  const safeAccounts = useMemo(() => (Array.isArray(accounts) ? accounts : []), [accounts]);
  const filteredTransactions = useMemo(
    () => (Array.isArray(transactions) ? transactions : []),
    [transactions]
  );

  return (
    <section>
      <h2>Customer Dashboard</h2>
      <p className="muted">
        Welcome, {displayName}. As a customer, you can see your accounts, balances, currencies,
        and transactions for your profile.
      </p>

      <div className="card" style={{ marginBottom: "12px" }}>
        <h3>Your Profile</h3>
        <p>
          <strong>Name:</strong> {displayName}
        </p>
        <p>
          <strong>User ID:</strong> {user.id}
        </p>
      </div>

      <TabButtons
        tabs={[
          { id: "overview", label: "Overview" },
          { id: "accounts", label: "Accounts" },
          { id: "transfer", label: "Transfer / Transactions" },
        ]}
        current={tab}
        onChange={setTab}
      />

      {tab === "overview" && <SummaryCards accounts={accounts} />}

      {tab === "accounts" && (
        <>
          <h3>Your Accounts</h3>
          <AccountsTable accounts={accounts} />
        </>
      )}

      {tab === "transfer" && (
        <>
          <h3>Transfer / Deposit / Withdraw</h3>
          <TransferForm accounts={safeAccounts} onSuccess={onRefresh} />

          <div className="grid-2" style={{ marginTop: "12px" }}>
            <QuickTransactionForm type="deposit" accounts={safeAccounts} onRefresh={onRefresh} />
            <QuickTransactionForm type="withdraw" accounts={safeAccounts} onRefresh={onRefresh} />
          </div>

          <h3 style={{ marginTop: "16px" }}>Recent Transactions</h3>
          <TransactionsTable transactions={filteredTransactions} />
        </>
      )}
    </section>
  );
}

function TabButtons({ tabs, current, onChange }) {
  return (
    <div style={{ marginBottom: "12px" }}>
      {tabs.map((t) => (
        <button
          key={t.id}
          className={current === t.id ? "nav-btn active" : "nav-btn"}
          style={{ marginRight: "6px" }}
          onClick={() => onChange(t.id)}
        >
          {t.label}
        </button>
      ))}
    </div>
  );
}

function QuickTransactionForm({ type, accounts, onRefresh }) {
  const [accountId, setAccountId] = useState("");
  const [amount, setAmount] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const isDeposit = type === "deposit";
  const title = isDeposit ? "Deposit" : "Withdraw";

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      setLoading(true);
      await recordTransaction({
        fromAccount: isDeposit ? undefined : accountId,
        toAccount: isDeposit ? accountId : undefined,
        amount: Number(amount),
        type,
      });
      setMessage(`${title} of $${amount || 0} ${isDeposit ? "to" : "from"} ${accountId} submitted.`);
      setAmount("");
      setAccountId("");
      if (onRefresh) onRefresh();
    } catch (err) {
      setError(err.message || `${title} failed`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="card form-card" onSubmit={onSubmit}>
      <h4>{title}</h4>
      <div className="field">
        <span>Account</span>
        <select value={accountId} onChange={(e) => setAccountId(e.target.value)} required>
          <option value="">Select account</option>
          {(accounts || []).map((a) => (
            <option key={a.id} value={a.id}>
              {a.id} ({a.type})
            </option>
          ))}
        </select>
      </div>
      <div className="field">
        <span>Amount</span>
        <input
          type="number"
          min="0"
          step="0.01"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          placeholder="0.00"
          required
        />
      </div>
      <button type="submit" disabled={loading}>
        {loading ? "Submitting..." : title}
      </button>
      {message ? <p className="muted">{message}</p> : null}
      {error ? <p className="error-msg">{error}</p> : null}
    </form>
  );
}
