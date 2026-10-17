import axios from "axios";

// One shared Axios instance. Every page imports this instead of creating its own.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
});

// Person 2: when login exists, attach the JWT here so every request carries it.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default api;
