import React, { useState } from "react";
import { createTeller, deleteTeller } from "../api";
import SummaryCards from "../components/SummaryCards.jsx";
import AccountsTable from "../components/AccountsTable.jsx";
import TransactionsTable from "../components/TransactionsTable.jsx";
import SearchForm from "../components/SearchForm.jsx";

export default function AdminDashboard({ user, accounts, transactions }) {
  const [tab, setTab] = useState("overview"); // overview | accounts | search | manage

  return (
    <section>
      <h2>Database Admin Dashboard</h2>
      <p className="muted">
        Welcome, {user.name}. As a database admin, you manage tellers, customers,
        branches and can view system activity.
      </p>
      <div className="card" style={{ marginBottom: "12px" }}>
        <h3>Your Profile</h3>
        <p>
          <strong>Name:</strong> {user.name}
        </p>
        <p>
          <strong>User ID:</strong> {user.id}
        </p>
      </div>

      <TabButtons
        tabs={[
          { id: "overview", label: "Overview" },
          { id: "accounts", label: "Accounts" },
          { id: "search", label: "Search" },
          { id: "manage", label: "Manage Users & Branches" },
        ]}
        current={tab}
        onChange={setTab}
      />

      {tab === "overview" && (
        <>
          <SummaryCards accounts={accounts} />
          <h3>System Activity (Sample)</h3>
          <TransactionsTable transactions={transactions} />
        </>
      )}

      {tab === "accounts" && (
        <>
          <h3>All Accounts (Sample)</h3>
          <AccountsTable accounts={accounts} />
        </>
      )}

      {tab === "search" && (
        <>
          <h3>Search Accounts / Users</h3>
          <SearchForm />
          <h3 style={{ marginTop: "16px" }}>Results (sample)</h3>
          <AccountsTable accounts={accounts} />
        </>
      )}

      {tab === "manage" && (
        <>
          <h3>Manage Users & Branches (Prototype)</h3>
          <p className="muted">
            These forms show how the admin would manage tellers and branches. You can
            connect them to your Java backend later.
          </p>

          <div className="grid-2">
            <TellerManagement />
            <BranchManagement />
          </div>
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

function TellerManagement() {
  const [form, setForm] = useState({
    tellerId: "",
    username: "",
    password: "",
    branch: "",
    action: "open",
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const onChange = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");
    try {
      setLoading(true);
      if (form.action === "open") {
        await createTeller({
          bankTellerID: form.tellerId,
          username: form.username,
          password: form.password,
          branchID: form.branch,
        });
        setMessage("Teller account created.");
      } else {
        await deleteTeller(form.tellerId);
        setMessage("Teller account closed.");
      }
    } catch (err) {
      setError(err.message || "Failed to process teller request");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="card form-card" onSubmit={onSubmit}>
      <h4>Open / Close Bank Teller Account</h4>
      <div className="field">
        <span>Action</span>
        <select value={form.action} onChange={(e) => onChange("action", e.target.value)}>
          <option value="open">Open teller account</option>
          <option value="close">Close teller account</option>
        </select>
      </div>
      <div className="field">
        <span>Teller ID</span>
        <input
          value={form.tellerId}
          onChange={(e) => onChange("tellerId", e.target.value)}
          placeholder="e.g. TELLER-01"
        />
      </div>
      <div className="field">
        <span>Username</span>
        <input
          value={form.username}
          onChange={(e) => onChange("username", e.target.value)}
          placeholder="login username"
        />
      </div>
      <div className="field">
        <span>Password</span>
        <input
          type="password"
          value={form.password}
          onChange={(e) => onChange("password", e.target.value)}
          placeholder="login password"
        />
      </div>
      <div className="field">
        <span>Branch</span>
        <input
          value={form.branch}
          onChange={(e) => onChange("branch", e.target.value)}
          placeholder="e.g. Downtown"
        />
      </div>
      <button type="submit" disabled={loading}>
        {form.action === "open"
          ? loading ? "Opening..." : "Open teller account"
          : loading ? "Closing..." : "Close teller account"}
      </button>
      {message ? <p className="muted" style={{ marginTop: "8px" }}>{message}</p> : null}
      {error ? <p className="error-msg" style={{ marginTop: "8px" }}>{error}</p> : null}
    </form>
  );
}

function BranchManagement() {
  return (
    <form className="card form-card">
      <h4>Create / Update Branch</h4>
      <div className="field">
        <span>Branch ID</span>
        <input placeholder="e.g. BR-01" />
      </div>
      <div className="field">
        <span>Branch Name</span>
        <input placeholder="e.g. Lakeside" />
      </div>
      <div className="field">
        <span>Address</span>
        <input placeholder="Street, City" />
      </div>
      <button type="button">Save Branch</button>
    </form>
  );
}
