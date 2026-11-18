import api from "./client.js";

// Log in. Returns { id, name, email, token }.
export async function login(email, password) {
  const res = await api.post("/auth/login", { email, password });
  return res.data;
}

// Create an account. Returns { id, name, email } (no token, so log in afterwards).
export async function register(name, email, password) {
  const res = await api.post("/auth/register", { name, email, password });
  return res.data;
}
