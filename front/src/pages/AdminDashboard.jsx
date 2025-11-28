import React, { useState, useEffect } from "react";
import { createTeller, deleteTeller, searchAccounts, fetchBranches, fetchBanks, createBranch, fetchTellers } from "../api";
import SummaryCards from "../components/SummaryCards.jsx";
import AccountsTable from "../components/AccountsTable.jsx";
import TransactionsTable from "../components/TransactionsTable.jsx";
import SearchForm from "../components/SearchForm.jsx";

export default function AdminDashboard({ user, accounts, transactions }) {
  const [tab, setTab] = useState("overview"); // overview | accounts | search | manage
  const [searchResults, setSearchResults] = useState(accounts || []);
  const [searchError, setSearchError] = useState("");
  const [searchLoading, setSearchLoading] = useState(false);
  const hasResults = Array.isArray(searchResults) && searchResults.length > 0;
  const [banks, setBanks] = useState([]);
  const [branches, setBranches] = useState([]);
  const [tellers, setTellers] = useState([]);

  const refreshMeta = () => {
    fetchBranches().then(setBranches).catch(() => {});
    fetchBanks().then(setBanks).catch(() => {});
    fetchTellers().then(setTellers).catch(() => {});
  };

  useEffect(() => {
    refreshMeta();
  }, []);

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
          { id: "banks", label: "Banks / Branches / Tellers" },
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

      {tab === "manage" && (
        <>
          <h3>Manage Users & Branches (Prototype)</h3>
          <p className="muted">
            These forms show how the admin would manage tellers and branches. You can
            connect them to your Java backend later.
          </p>

          <div className="grid-2">
            <TellerManagement branches={branches} onDataChanged={refreshMeta} />
            <BranchManagement branches={branches} onBranchCreated={refreshMeta} />
          </div>
        </>
      )}

      {tab === "banks" && (
        <BankBranchTeller banks={banks} branches={branches} tellers={tellers} />
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

function TellerManagement({ branches, onDataChanged }) {
  const [form, setForm] = useState({
    tellerId: "",
    username: "",
    password: "",
    branch: "",
    action: "open",
    firstName: "",
    lastName: "",
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
          bankTellerID: form.tellerId || undefined,
          username: form.username,
          password: form.password,
          branchID: form.branch,
          firstName: form.firstName,
          lastName: form.lastName,
        });
        setMessage("Teller account created.");
        if (onDataChanged) onDataChanged();
      } else {
        if (!form.tellerId) {
          throw new Error("Teller ID is required to close an account");
        }
        await deleteTeller(form.tellerId);
        setMessage("Teller account closed.");
        if (onDataChanged) onDataChanged();
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
      {form.action === "close" && (
        <div className="field">
          <span>Teller ID</span>
          <input
            value={form.tellerId}
            onChange={(e) => onChange("tellerId", e.target.value)}
            placeholder="e.g. BT001"
            required
          />
        </div>
      )}
      {form.action === "open" && (
        <>
          <div className="field">
            <span>First name</span>
            <input
              value={form.firstName}
              onChange={(e) => onChange("firstName", e.target.value)}
              placeholder="e.g. John"
              required
            />
          </div>
          <div className="field">
            <span>Last name</span>
            <input
              value={form.lastName}
              onChange={(e) => onChange("lastName", e.target.value)}
              placeholder="e.g. Doe"
              required
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
            <select value={form.branch} onChange={(e) => onChange("branch", e.target.value)} required>
              <option value="">Select branch</option>
              {branches.map((b) => (
                <option key={b.branchID} value={b.branchID}>
                  {b.branchID} - {b.branchName}
                </option>
              ))}
            </select>
          </div>
        </>
      )}
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

function BranchManagement({ onBranchCreated }) {
  const [banks, setBanks] = useState([]);
  const [form, setForm] = useState({
    bankID: "",
    branchName: "",
    address: "",
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchBanks().then(setBanks).catch(() => {});
  }, []);

  const onChange = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const onSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");
    try {
      setLoading(true);
      await createBranch({
        bankID: form.bankID,
        branchName: form.branchName,
        address: form.address,
      });
      setMessage("Branch created.");
      setForm({ bankID: "", branchName: "", address: "" });
      if (onBranchCreated) onBranchCreated();
    } catch (err) {
      setError(err.message || "Failed to create branch");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="card form-card" onSubmit={onSubmit}>
      <h4>Create Branch</h4>
      <div className="field">
        <span>Bank</span>
        <select value={form.bankID} onChange={(e) => onChange("bankID", e.target.value)} required>
          <option value="">Select bank</option>
          {banks.map((b) => (
            <option key={b.bankID} value={b.bankID}>
              {b.bankID} - {b.name}
            </option>
          ))}
        </select>
      </div>
      <div className="field">
        <span>Branch Name</span>
        <input
          value={form.branchName}
          onChange={(e) => onChange("branchName", e.target.value)}
          placeholder="e.g. Uptown"
          required
        />
      </div>
      <div className="field">
        <span>Address</span>
        <input
          value={form.address}
          onChange={(e) => onChange("address", e.target.value)}
          placeholder="Street, City"
          required
        />
      </div>
      <button type="submit" disabled={loading}>
        {loading ? "Saving..." : "Save Branch"}
      </button>
      {message ? <p className="muted" style={{ marginTop: "8px" }}>{message}</p> : null}
      {error ? <p className="error-msg" style={{ marginTop: "8px" }}>{error}</p> : null}
    </form>
  );
}

// Collapsible view of banks -> branches -> tellers
function BankBranchTeller({ banks, branches, tellers }) {
  const bankList = banks || [];
  const branchMap = new Map((branches || []).map((b) => [b.branchID, b]));

  if (!bankList.length) {
    return <p className="muted">No banks found.</p>;
  }

  return (
    <div className="card">
      <h4>Banks, Branches, and Tellers</h4>
      {bankList.map((bank) => {
        const bankBranches = (bank.branches || []).map((bid) => branchMap.get(bid)).filter(Boolean);
        return (
          <details key={bank.bankID} style={{ marginBottom: "10px" }} open>
            <summary style={{ cursor: "pointer", fontWeight: 600 }}>
              {bank.bankID} - {bank.name}
            </summary>
            {bankBranches.length === 0 ? (
              <p className="muted" style={{ marginLeft: "16px" }}>No branches for this bank.</p>
            ) : (
              bankBranches.map((br) => {
                // Normalize tellers: if branch has teller IDs or objects, map to full teller objects when possible
                let brTellers = [];
                if (br.tellers && br.tellers.length) {
                  brTellers = br.tellers
                    .map((tid) => {
                      if (typeof tid === "string") {
                        return (tellers || []).find((t) => t.bankTellerID === tid) || null;
                      }
                      return tid;
                    })
                    .filter(Boolean);
                } else {
                  brTellers = (tellers || []).filter((t) => t.branchID === br.branchID);
                }
                return (
                  <details key={br.branchID} style={{ marginLeft: "16px", marginTop: "6px" }} open>
                    <summary style={{ cursor: "pointer" }}>
                      {br.branchID} - {br.branchName} ({br.address || "No address"})
                    </summary>
                    {brTellers.length === 0 ? (
                      <p className="muted" style={{ marginLeft: "16px" }}>No tellers for this branch.</p>
                    ) : (
                      <div className="table-wrapper" style={{ marginLeft: "16px" }}>
                        <table>
                          <thead>
                            <tr>
                              <th>Teller ID</th>
                              <th>First name</th>
                              <th>Last name</th>
                              <th>Username</th>
                            </tr>
                          </thead>
                          <tbody>
                            {brTellers.map((t) => (
                              <tr key={t.bankTellerID}>
                                <td>{t.bankTellerID}</td>
                                <td>{t.firstName || t.firstname || "—"}</td>
                                <td>{t.lastName || t.lastname || "—"}</td>
                                <td>{t.username}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </details>
                );
              })
            )}
          </details>
        );
      })}
    </div>
  );
}
