import React from "react";

export default function Header({ user, onLogout }) {
  const displayName =
    user?.firstName && user?.lastName ? `${user.firstName} ${user.lastName}` : user?.name;

  return (
    <header className="top-bar">
      <div>
        <h1 className="logo">MyBankUML</h1>
        <p className="subtitle"> Web UI </p>
      </div>

      <div>
        {user ? (
          <div className="header-user">
            <span className="muted">
              Logged in as <strong>{displayName}</strong> ({user.role})
            </span>
            <button className="logout-btn" onClick={onLogout}>
              Logout
            </button>
          </div>
        ) : (
          <span className="muted">Not logged in</span>
        )}
      </div>
    </header>
  );
}
