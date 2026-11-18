import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api/auth.js";
import { saveLogin } from "../auth.js";

const inputClass =
  "mt-1 w-full rounded-lg border border-[var(--color-accent)] bg-[var(--color-bg)] px-3 py-2 text-[var(--color-text)]";

export default function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      const data = await login(email, password);
      saveLogin(data);
      navigate("/for-you");
    } catch (err) {
      setError(err.response?.data?.message || "Could not reach the server");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="text-3xl font-bold">Log in</h1>
      <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-4">
        <label className="text-sm">
          Email
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className={inputClass}
          />
        </label>
        <label className="text-sm">
          Password
          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className={inputClass}
          />
        </label>
        {error && (
          <p role="alert" className="font-medium">
            {error}
          </p>
        )}
        <button
          type="submit"
          disabled={busy}
          className="rounded-lg bg-[var(--color-accent)] px-4 py-2 font-semibold text-white disabled:opacity-60"
        >
          {busy ? "Logging in..." : "Log in"}
        </button>
      </form>
      <p className="mt-4 text-sm">
        New here? <Link to="/register" className="underline">Create an account</Link>
      </p>
    </main>
  );
}
