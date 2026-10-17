import { useEffect, useState } from "react";
import api from "../api/client.js";

// Placeholder home page. It calls the backend health endpoint to prove
// that the frontend and backend are connected. Person 1 replaces this page.
export default function Home() {
  const [status, setStatus] = useState("checking...");

  useEffect(() => {
    api.get("/health")
      .then((res) => setStatus(res.data.status))
      .catch(() => setStatus("backend not reachable"));
  }, []);

  return (
    <main className="mx-auto max-w-3xl p-8">
      <h1 className="text-3xl font-bold">Book Recommendation System</h1>
      <p className="mt-4">Backend status: {status}</p>
    </main>
  );
}
