import React from "react";

export default function TransactionsTable({ transactions }) {
  const rows = Array.isArray(transactions) ? transactions : [];
  return (
    <div className="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Date</th>
            <th>Type</th>
            <th>Source</th>
            <th>Destination</th>
            <th>Amount</th>
          </tr>
        </thead>
        <tbody>
          {rows.length > 0 ? (
            rows.map((tx) => (
              <tr key={tx.id}>
                <td>{tx.id}</td>
                <td>{tx.date || ""}</td>
                <td>{tx.type}</td>
                <td>{tx.account || tx.sourceAccountID || ""}</td>
                <td>{tx.receiverAccountID || ""}</td>
                <td>${Number(tx.amount ?? 0).toFixed(2)}</td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={6} style={{ textAlign: "center" }}>
                No transactions yet.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
