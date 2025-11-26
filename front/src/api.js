

const API_BASE = process.env.REACT_APP_API_BASE || "http://localhost:8080/api";

const normalizeAccount = (acct) => ({
  id: acct.id || acct.userID || acct.accountID,
  type: acct.type || acct.accountType || "Checking",
  balance: acct.balance ?? 0,
  branch: acct.branch || "",
  customerName: acct.customerName || acct.name || "",
  currency: acct.currency || acct.accountCurrency || "USD",
});

const normalizeTransaction = (tx) => ({
  id: tx.id || tx.transactionID || tx.transactionId,
  date: tx.date || tx.transactionDateTime || "",
  type: tx.type || tx.transactionType || "",
  amount: tx.amount ?? 0,
  account: tx.account || tx.sourceAccountID || tx.sourceAccountId || "",
  receiverAccountID: tx.receiverAccountID || tx.receiverAccountId || "",
  status: tx.status || "",
});

async function handleResponse(response) {
  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch (e) {
      // fall through
    }
  }
  if (!response.ok) {
    const message = data?.error || response.statusText || "Request failed";
    throw new Error(message);
  }
  return data;
}

export async function login(username, password) {
  console.log("Logging in user:", username);
  const res = await fetch(`${API_BASE}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  const data = await handleResponse(res);
  const firstName = data?.firstName;
  const lastName = data?.lastName;
  const fullName =
    firstName && lastName ? `${firstName} ${lastName}` : data?.name || username;
  return {
    name: fullName,
    firstName,
    lastName,
    role: data?.role,
    id: data?.id,
  };
}

export async function fetchAccounts(params = {}) {
  const url = new URL(`${API_BASE}/accounts`);
  Object.entries(params).forEach(([key, value]) => {
    if (value) url.searchParams.append(key, value);
  });
  const res = await fetch(url.toString());
  const data = await handleResponse(res);
  return Array.isArray(data) ? data.map(normalizeAccount) : [];
}

export async function searchAccounts(filters) {
  return fetchAccounts(filters);
}

export async function fetchTransactions(accountId) {
  const url = new URL(`${API_BASE}/transactions`);
  if (accountId) url.searchParams.append("accountId", accountId);
  const res = await fetch(url.toString());
  const data = await handleResponse(res);
  return Array.isArray(data) ? data.map(normalizeTransaction) : [];
}

export async function createCustomer(payload) {
  const res = await fetch(`${API_BASE}/customers`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

export async function deleteCustomer(userId) {
  const res = await fetch(`${API_BASE}/customers/${userId}`, {
    method: "DELETE",
  });
  return handleResponse(res);
}

export async function createTeller(payload) {
  const res = await fetch(`${API_BASE}/tellers`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

export async function deleteTeller(tellerId) {
  const res = await fetch(`${API_BASE}/tellers/${tellerId}`, {
    method: "DELETE",
  });
  return handleResponse(res);
}

export async function manageAccount(payload) {
  const res = await fetch(`${API_BASE}/accounts/manage`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}

export async function recordTransaction(payload) {
  const res = await fetch(`${API_BASE}/transactions/transfer`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
}
