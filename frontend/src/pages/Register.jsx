import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../api/auth.js";

export default function Register() {
    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [fieldErrors, setFieldErrors] = useState({});
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    function validate() {
        const nextErrors = {};
        const trimmedName = name.trim();
        const trimmedEmail = email.trim();

        if (!trimmedName) {
            nextErrors.name = "Name is required.";
        } else if (trimmedName.length < 2) {
            nextErrors.name = "Name must be at least 2 characters.";
        }

        if (!trimmedEmail) {
            nextErrors.email = "Email is required.";
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
            nextErrors.email = "Please enter a valid email address.";
        }

        if (!password) {
            nextErrors.password = "Password is required.";
        } else if (password.length < 6) {
            nextErrors.password = "Password must be at least 6 characters.";
        }

        setFieldErrors(nextErrors);
        return Object.keys(nextErrors).length === 0;
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");

        if (!validate()) {
            return;
        }

        setSubmitting(true);

        try {
            await registerUser(name.trim(), email.trim(), password);

            navigate("/login", {
                state: { message: "Registration successful! Please log in." },
            });
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                err.message ||
                "Registration failed. Please try again."
            );
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="mx-auto max-w-md p-8">
            <h1 className="text-3xl font-bold">Create an account</h1>
            <p className="mt-2" style={{ color: "var(--color-muted)" }}>
                Join us and discover your next favourite book.
            </p>

            <form onSubmit={handleSubmit} noValidate className="mt-6 space-y-4">
                <label className="block">
                    <span className="text-sm font-medium">Name</span>
                    <input
                        type="text"
                        value={name}
                        onChange={(e) => {
                            setName(e.target.value);
                            setFieldErrors((prev) => ({ ...prev, name: "" }));
                        }}
                        className="mt-1 w-full rounded-lg border p-3"
                        style={{
                            background: "var(--color-surface)",
                            color: "var(--color-text)",
                            borderColor: "var(--color-border)",
                        }}
                    />
                    {fieldErrors.name && (
                        <p role="alert" className="mt-1 text-sm text-red-600">
                            {fieldErrors.name}
                        </p>
                    )}
                </label>

                <label className="block">
                    <span className="text-sm font-medium">Email</span>
                    <input
                        type="email"
                        autoComplete="email"
                        value={email}
                        onChange={(e) => {
                            setEmail(e.target.value);
                            setFieldErrors((prev) => ({ ...prev, email: "" }));
                        }}
                        className="mt-1 w-full rounded-lg border p-3"
                        style={{
                            background: "var(--color-surface)",
                            color: "var(--color-text)",
                            borderColor: "var(--color-border)",
                        }}
                    />
                    {fieldErrors.email && (
                        <p role="alert" className="mt-1 text-sm text-red-600">
                            {fieldErrors.email}
                        </p>
                    )}
                </label>

                <label className="block">
                    <span className="text-sm font-medium">Password</span>
                    <input
                        type="password"
                        autoComplete="new-password"
                        value={password}
                        onChange={(e) => {
                            setPassword(e.target.value);
                            setFieldErrors((prev) => ({ ...prev, password: "" }));
                        }}
                        className="mt-1 w-full rounded-lg border p-3"
                        style={{
                            background: "var(--color-surface)",
                            color: "var(--color-text)",
                            borderColor: "var(--color-border)",
                        }}
                    />
                    {fieldErrors.password && (
                        <p role="alert" className="mt-1 text-sm text-red-600">
                            {fieldErrors.password}
                        </p>
                    )}
                </label>

                {error && (
                    <p role="alert" className="text-sm text-red-600">
                        {error}
                    </p>
                )}

                <button
                    type="submit"
                    disabled={submitting}
                    className="w-full rounded-lg p-3 font-semibold disabled:opacity-60"
                    style={{
                        background: "var(--color-accent)",
                        color: "var(--color-accent-text)",
                    }}
                >
                    {submitting ? "Creating account..." : "Register"}
                </button>
            </form>

            <p className="mt-5 text-sm">
                Already have an account?{" "}
                <Link to="/login" className="font-semibold underline">
                    Log in
                </Link>
            </p>
        </main>
    );
}