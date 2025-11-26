import React, { useState } from "react";

export default function TransferForm({ accounts, forTeller }) {
  const [fromAcc, setFromAcc] = useState("");
  const [toAcc, setToAcc] = useState("");
  const [amount, setAmount] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    // TODO: call backend here (POST /transactions)
    setMessage(
      `Transfer of $${amount} from ${fromAcc} to ${toAcc} ${
        forTeller ? "(recorded by teller)" : ""
      }`
    );
  };

  return (
    <form className="card form-card" onSubmit={handleSubmit}>
      <div className="field">
        <span>From Account</span>
        <select value={fromAcc} onChange={(e) => setFromAcc(e.target.value)} required>
          <option value="">Select account</option>
          {accounts.map((a) => (
            <option key={a.id} value={a.id}>
              {a.id} ({a.type})
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <span>To Account</span>
        <input
          value={toAcc}
          onChange={(e) => setToAcc(e.target.value)}
          placeholder="Target account ID"
          required
        />
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

      <button type="submit">Submit</button>

      {message && <p className="success-msg">{message}</p>}
    </form>
  );
}
