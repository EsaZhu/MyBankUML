import React, { useState } from "react";
import { recordTransaction } from "../api";

export default function TransferForm({ accounts, forTeller, onSuccess }) {
  const [fromAcc, setFromAcc] = useState("");
  const [toAcc, setToAcc] = useState("");
  const [amount, setAmount] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    setLoading(true);
    try {
      await recordTransaction({
        fromAccount: fromAcc,
        toAccount: toAcc,
        amount: Number(amount),
        type: "transfer",
      });
      setMessage(
        `Transfer of $${amount} from ${fromAcc} to ${toAcc} ${forTeller ? "(recorded by teller)" : ""}`
      );
      setAmount("");
      setFromAcc("");
      setToAcc("");
      if (onSuccess) onSuccess();
    } catch (err) {
      setError(err.message || "Transfer failed");
    } finally {
      setLoading(false);
    }
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
        <select value={toAcc} onChange={(e) => setToAcc(e.target.value)} required>
          <option value="">Select account</option>
          {accounts.map((a) => (
            <option key={`${a.id}-to`} value={a.id}>
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
        {loading ? "Submitting..." : "Submit"}
      </button>

      {message && <p className="success-msg">{message}</p>}
      {error && <p className="error-msg">{error}</p>}
    </form>
  );
}
