import React from "react";

export default function AccountsTable({ accounts }) {
  return (
    <div className="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Account ID</th>
            <th>Type</th>
            <th>Currency</th>
            <th>Branch</th>
            <th>Balance</th>
          </tr>
        </thead>
        <tbody>
          {accounts.map((acc) => (
            <tr key={acc.id}>
              <td>{acc.id}</td>
              <td>{acc.type}</td>
              <td>{acc.currency || "USD"}</td>
              <td>{acc.branch}</td>
              <td>${Number(acc.balance ?? 0).toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
