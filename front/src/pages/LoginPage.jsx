import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login as apiLogin } from "../api";

const ROLES = {
  CUSTOMER: "CUSTOMER",
  TELLER: "TELLER",
  ADMIN: "ADMIN",
};

const getRouteForRole = (role) => {
  if (role === ROLES.CUSTOMER) return "/customer";
  if (role === ROLES.TELLER) return "/teller";
  if (role === ROLES.ADMIN) return "/admin";
  return "/login";
};

export default function LoginPage({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (!username.trim() || !password) {
      setError("Username and password are required.");
      return;
    }

    setLoading(true);
    try {
      const user = await apiLogin(username.trim(), password);
      if (!user.role) {
        throw new Error("No role returned from server");
      }
      onLogin(user);
      navigate(getRouteForRole(user.role));
    } catch (err) {
      setError(err.message || "Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="center-wrapper">
      <div className="card login-card">
        <h2>Login</h2>
        <p className="muted">
          Sign in to access your MyBank account.
        </p>

        <form onSubmit={handleSubmit} className="login-form">
          <label className="field">
            <span>Username</span>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="e.g. customer"
              autoComplete="username"
            />
          </label>

          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              autoComplete="current-password"
            />
          </label>

          {error ? <div className="error-msg">{error}</div> : null}

          <button type="submit" disabled={loading || !username.trim() || !password}>
            {loading ? "Checking..." : "Sign in"}
          </button>

          <p className="muted small">
            Your credentials will be verified securely against the MyBank database.
            Please enter your valid username and password to continue.
          </p>
        </form>
      </div>
    </div>
  );
}
