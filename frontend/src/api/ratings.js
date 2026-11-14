import api from "./client.js";

// Returns up to 1000 of a user's ratings (the Profile page can page through them later).
export async function getUserRatings(userId, size = 1000) {
    const res = await api.get(`/users/${userId}/ratings`, { params: { size } });
    return res.data.content;
}

// Rates a book for the first time. Fails with 409 if the user already rated it.
export async function createRating(userId, bookId, score) {
    const res = await api.post("/ratings", { userId, bookId, score });
    return res.data;
}

// Changes the score of an existing rating.
export async function updateRating(ratingId, score) {
    const res = await api.put(`/ratings/${ratingId}`, { score });
    return res.data;
}

// Removes a rating.
export async function deleteRating(ratingId) {
    await api.delete(`/ratings/${ratingId}`);
}