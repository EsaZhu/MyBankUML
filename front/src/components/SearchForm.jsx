import React, { useState } from "react";

export default function SearchForm({ onSearch, loading }) {
  const [accountId, setAccountId] = useState("");
  const [name, setName] = useState("");
  const [branch, setBranch] = useState("");
  const [type, setType] = useState("");

  const handleSearch = (e) => {
    e.preventDefault();
    onSearch({
      accountId: accountId.trim(),
      name: name.trim(),
      branch: branch.trim(),
      type,
    });
  };

  return (
    <form className="card form-card" onSubmit={handleSearch}>
      <div className="grid-2">
        <div className="field">
          <span>Account ID</span>
          <input
            value={accountId}
            onChange={(e) => setAccountId(e.target.value)}
            placeholder="e.g. CHK-1001"
          />
        </div>
        <div className="field">
          <span>Customer Name</span>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. John Doe"
          />
        </div>
        <div className="field">
          <span>Branch</span>
          <input
            value={branch}
            onChange={(e) => setBranch(e.target.value)}
            placeholder="e.g. Downtown"
          />
        </div>
        <div className="field">
          <span>Account Type</span>
          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="">Any</option>
            <option value="Checking">Checking</option>
            <option value="Savings">Savings</option>
            <option value="Card">Card</option>
          </select>
        </div>
      </div>

      <button type="submit" disabled={loading}>
        {loading ? "Searching..." : "Search"}
      </button>
    </form>
  );
}
