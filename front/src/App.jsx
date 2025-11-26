import React, { useCallback, useEffect, useState } from "react";
import { Routes, Route, Navigate } from "react-router-dom";

import Header from "./components/Header.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";

import LoginPage from "./pages/LoginPage.jsx";
import CustomerDashboard from "./pages/CustomerDashboard.jsx";
import TellerDashboard from "./pages/TellerDashboard.jsx";
import AdminDashboard from "./pages/AdminDashboard.jsx";
import TellerCustomerProfile from "./pages/TellerCustomerProfile.jsx";
import { fetchAccounts, fetchTransactions } from "./api";

const ROLES = {
  CUSTOMER: "CUSTOMER",
  TELLER: "TELLER",
  ADMIN: "ADMIN",
};

export default function App() {
  const [user, setUser] = useState(null); 
  // user = { name: "", role: "", id: "" } or null

  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loadingData, setLoadingData] = useState(false);
  const [dataError, setDataError] = useState("");

  const handleLogin = (userData) => {
    setUser(userData);
  };

  const handleLogout = () => {
    setUser(null);
  };

  const getRouteForRole = (role) => {
    if (role === ROLES.CUSTOMER) return "/customer";
    if (role === ROLES.TELLER) return "/teller";
    if (role === ROLES.ADMIN) return "/admin";
    return "/login";
  };

  const refreshData = useCallback(async () => {
    if (!user) {
      setAccounts([]);
      setTransactions([]);
      return;
    }
    setLoadingData(true);
    setDataError("");
    try {
      const accountParams = user.role === ROLES.CUSTOMER ? { accountId: user.id } : {};
      const [acctList, txList] = await Promise.all([
        fetchAccounts(accountParams),
        fetchTransactions(user.role === ROLES.CUSTOMER ? user.id : undefined),
      ]);
      setAccounts(acctList);
      setTransactions(txList);
    } catch (err) {
      setDataError(err.message || "Failed to load data from server");
    } finally {
      setLoadingData(false);
    }
  }, [user]);

  useEffect(() => {
    refreshData();
  }, [refreshData]);

  return (
    <div className="app">
      <Header user={user} onLogout={handleLogout} />

      <main className="content">
        {dataError ? <div className="error-msg" style={{ marginBottom: "10px" }}>{dataError}</div> : null}
        {loadingData ? <p className="muted">Loading data...</p> : null}
        <Routes>
          {/* Default route */}
          <Route
            path="/"
            element={
              user ? (
                <Navigate to={getRouteForRole(user.role)} replace />
              ) : (
                <Navigate to="/login" replace />
              )
            }
          />

          {/* Login */}
          <Route
            path="/login"
            element={
              user ? (
                <Navigate to={getRouteForRole(user.role)} replace />
              ) : (
                <LoginPage onLogin={handleLogin} />
              )
            }
          />

          {/* Customer */}
          <Route
            path="/customer"
            element={
              <ProtectedRoute user={user} allowedRoles={[ROLES.CUSTOMER]}>
                <CustomerDashboard
                  user={user}
                  accounts={accounts}
                  transactions={transactions}
                  onRefresh={refreshData}
                />
              </ProtectedRoute>
            }
          />

          {/* Teller */}
          <Route
            path="/teller"
            element={
              <ProtectedRoute user={user} allowedRoles={[ROLES.TELLER]}>
                <TellerDashboard user={user} accounts={accounts} transactions={transactions} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/teller/customers/:accountId"
            element={
              <ProtectedRoute user={user} allowedRoles={[ROLES.TELLER]}>
                <TellerCustomerProfile accounts={accounts} transactions={transactions} />
              </ProtectedRoute>
            }
          />

          {/* Admin */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute user={user} allowedRoles={[ROLES.ADMIN]}>
                <AdminDashboard user={user} accounts={accounts} transactions={transactions} />
              </ProtectedRoute>
            }
          />

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
