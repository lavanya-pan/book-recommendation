import api from "./client.js";

export async function getMyProfile() {
    const response = await api.get("/users/me");
    return response.data;
}

export async function updateMyReaderType(readerType) {
    const response = await api.put("/users/me/reader-type", { readerType });
    return response.data;
}

export async function updateMyProfile(name, email) {
    const response = await api.put("/users/me", { name, email });
    return response.data;
}