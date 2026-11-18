
import api from "./client.js";

export async function getWishlist(userId) {
    const response = await api.get("/wishlist", {
        params: { userId },
    });
    return response.data;
}

export async function addToWishlist(userId, bookId) {
    const response = await api.post("/wishlist", {
        userId,
        bookId,
    });
    return response.data;
}

export async function removeFromWishlist(userId, bookId) {
    const response = await api.delete(`/wishlist/${bookId}`, {
        params: { userId },
    });
    return response.data;
}
