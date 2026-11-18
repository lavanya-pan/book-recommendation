
package com.sds.bookrecommendation.dto;

import jakarta.validation.constraints.NotNull;

public class WishlistRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long bookId;

    public WishlistRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}
