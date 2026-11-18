// Keeps the logged-in reader in the browser. client.js sends the token on every request.
const TOKEN_KEY = "token";
const USER_KEY = "user";

// Call this with the response of the login request.
export function saveLogin(data) {
  localStorage.setItem(TOKEN_KEY, data.token);
  localStorage.setItem(
    USER_KEY,
    JSON.stringify({ id: data.id, name: data.name, email: data.email })
  );
}

export function logout() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function isLoggedIn() {
  return Boolean(localStorage.getItem(TOKEN_KEY));
}

// The logged-in reader ({ id, name, email }), or null.
export function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY));
  } catch {
    return null;
  }
}
