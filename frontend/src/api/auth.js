import api from "./client.js";

export async function registerUser(name, email, password) {
    const response = await api.post("/auth/register", {
        name,
        email,
        password,
    });
    return response.data;
}

export async function loginUser(email, password) {
    const response = await api.post("/auth/login", {
        email,
        password,
    });
    return response.data;
}