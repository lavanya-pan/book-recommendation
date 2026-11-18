
import api from "./client.js";

export async function getReadingHistory(userId) {
    const response = await api.get("/reading-history", {
        params: { userId },
    });
    return response.data;
}

export async function addToReadingHistory(userId, bookId, status) {
    const response = await api.post("/reading-history", {
        userId,
        bookId,
        status,
    });
    return response.data;
}

export async function updateReadingStatus(userId, bookId, status) {
    const response = await api.put(
        `/reading-history/${bookId}`,
        { status },
        { params: { userId } }
    );
    return response.data;
}
