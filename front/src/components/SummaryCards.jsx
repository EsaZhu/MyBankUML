import React from "react";

export default function SummaryCards({ accounts }) {
  const totalBalance = accounts.reduce((sum, a) => sum + a.balance, 0);

  return (
    <div className="summary-cards">
      <div className="card">
        <h3>Total Balance</h3>
        <p className="big-number">${totalBalance.toFixed(2)}</p>
      </div>
      <div className="card">
        <h3>Number of Accounts</h3>
        <p className="big-number">{accounts.length}</p>
      </div>
    </div>
  );
}
