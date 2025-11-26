import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { searchAccounts } from "../api";
import SearchForm from "../components/SearchForm.jsx";

export default function TellerDashboard({ user, accounts, transactions }) {
  const navigate = useNavigate();
  const [tab, setTab] = useState("createCustomer"); // createCustomer | transfer | search
  const [searchResults, setSearchResults] = useState(accounts || []);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState("");

  const hasResults = useMemo(() => searchResults && searchResults.length > 0, [searchResults]);

  useEffect(() => {
    setSearchResults(accounts || []);
  }, [accounts]);

  const handleSearch = async (filters) => {
    setSearchError("");
    setSearchLoading(true);
    try {
      const results = await searchAccounts(filters);
      setSearchResults(results);
    } catch (err) {
      setSearchError(err.message || "Search failed. Please try again.");
    } finally {
      setSearchLoading(false);
    }
  };

  const openCustomerProfile = (account) => {
    navigate(`/teller/customers/${account.id}`, { state: { account } });
  };

  return (
    <section>
      <h2>Bank Teller Dashboard</h2>
      <p className="muted">
        Welcome, {user.name}. As a bank teller, you can create customers, search accounts,
        and record transactions.
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
          { id: "createCustomer", label: "Create Customer" },
          { id: "search", label: "Search" },
        ]}
        current={tab}
        onChange={setTab}
      />

      {tab === "createCustomer" && (
        <div className="card form-card">
          <h3>Create a Customer</h3>
          <p className="muted">
            Capture customer details and open their primary account. Hook this up to the
            backend customer/account creation endpoint.
          </p>
          <CreateCustomerForm />

          <div className="card" style={{ marginTop: "12px" }}>
            <h4>Account Maintenance</h4>
            <p className="muted">
              Open or close a customer account. Wire these actions to the teller backend
              endpoints.
            </p>
            <AccountMaintenance />
          </div>
        </div>
      )}

     

      {tab === "search" && (
        <>
          <h3>Search Customers / Accounts</h3>
          <SearchForm onSearch={handleSearch} loading={searchLoading} />
          {searchError ? <div className="error-msg" style={{ marginTop: "10px" }}>{searchError}</div> : null}
          <h3 style={{ marginTop: "16px" }}>Results</h3>
          {hasResults ? (
            <div className="card table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Account ID</th>
                    <th>Customer Name</th>
                    <th>Type</th>
                    <th>Branch</th>
                    <th>Balance</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {searchResults.map((acct) => (
                    <tr key={acct.id}>
                      <td>{acct.id}</td>
                      <td>{acct.customerName || "—"}</td>
                      <td>{acct.type}</td>
                      <td>{acct.branch}</td>
                      <td>${acct.balance?.toLocaleString() ?? "—"}</td>
                      <td>
                        <button className="link-btn" onClick={() => openCustomerProfile(acct)}>
                          View customer
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="muted">No results yet. Enter criteria and search.</p>
          )}
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

function CreateCustomerForm() {
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    username: "",
    password: "",
    email: "",
    phone: "",
    branch: "",
    accountType: "Checking",
    initialDeposit: "",
  });
  const [message, setMessage] = useState("");

  const onChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const onSubmit = (e) => {
    e.preventDefault();
    setMessage(
      "Submitted. Hook this to the backend to create the customer and account in the database."
    );
  };

  return (
    <form className="grid-2" onSubmit={onSubmit}>
      <div className="field">
        <span>First name</span>
        <input
          value={form.firstName}
          onChange={(e) => onChange("firstName", e.target.value)}
          placeholder="e.g. Jane"
        />
      </div>
      <div className="field">
        <span>Last name</span>
        <input
          value={form.lastName}
          onChange={(e) => onChange("lastName", e.target.value)}
          placeholder="e.g. Doe"
        />
      </div>
      <div className="field">
        <span>Username</span>
        <input
          value={form.username}
          onChange={(e) => onChange("username", e.target.value)}
          placeholder="login username"
          required
        />
      </div>
      <div className="field">
        <span>Password</span>
        <input
          type="password"
          value={form.password}
          onChange={(e) => onChange("password", e.target.value)}
          placeholder="login password"
          required
        />
      </div>
     
      <div className="field">
        <span>Branch</span>
        <input
          value={form.branch}
          onChange={(e) => onChange("branch", e.target.value)}
          placeholder="Downtown"
        />
      </div>
      <div className="field">
        <span>Account type</span>
        <select
          value={form.accountType}
          onChange={(e) => onChange("accountType", e.target.value)}
        >
          <option value="Checking">Checking</option>
          <option value="Savings">Savings</option>
          <option value="Card">Card</option>
        </select>
      </div>
      <div className="field">
        <span>Initial deposit</span>
        <input
          type="number"
          value={form.initialDeposit}
          onChange={(e) => onChange("initialDeposit", e.target.value)}
          placeholder="0.00"
          min="0"
          step="0.01"
        />
      </div>
      <div style={{ display: "flex", alignItems: "flex-end" }}>
        <button type="submit">Create customer</button>
      </div>
      {message ? <p className="muted" style={{ gridColumn: "1 / -1" }}>{message}</p> : null}
    </form>
  );
}

function AccountMaintenance() {
  const [action, setAction] = useState("open");
  const [userId, setUserId] = useState("");
  const [accountType, setAccountType] = useState("Checking");
  const [message, setMessage] = useState("");

  const onSubmit = (e) => {
    e.preventDefault();
    setMessage(
      `${action === "open" ? "Open" : "Close"} account request for client ${
        userId || "N/A"
      } (${accountType}) submitted. Hook this to teller backend.`
    );
  };

  return (
    <form className="grid-2" onSubmit={onSubmit}>
      <div className="field">
        <span>Action</span>
        <select value={action} onChange={(e) => setAction(e.target.value)}>
          <option value="open">Open account</option>
          <option value="close">Close account</option>
        </select>
      </div>
      <div className="field">
        <span>Client User ID</span>
        <input
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          placeholder="e.g. U001"
          required
        />
      </div>
      <div className="field">
        <span>Account type</span>
        <select value={accountType} onChange={(e) => setAccountType(e.target.value)}>
          <option value="Checking">Checking</option>
          <option value="Savings">Savings</option>
          <option value="Card">Card</option>
        </select>
      </div>
      <div style={{ display: "flex", alignItems: "flex-end" }}>
        <button type="submit">{action === "open" ? "Open account" : "Close account"}</button>
      </div>
      {message ? <p className="muted" style={{ gridColumn: "1 / -1" }}>{message}</p> : null}
    </form>
  );
}
