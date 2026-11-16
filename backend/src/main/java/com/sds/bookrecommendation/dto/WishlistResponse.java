package com.sds.bookrecommendation.dto;

import com.sds.bookrecommendation.entity.WishlistItem;

import java.time.LocalDateTime;

public class WishlistResponse {

    private Long id;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private String author;
    private String genre;
    private String coverUrl;
    private LocalDateTime addedAt;

    public static WishlistResponse from(WishlistItem item) {
        WishlistResponse response = new WishlistResponse();

        response.id = item.getId();
        response.userId = item.getUser().getId();
        response.bookId = item.getBook().getId();
        response.bookTitle = item.getBook().getTitle();
        response.author = item.getBook().getAuthor();
        response.genre = item.getBook().getGenre();
        response.coverUrl = item.getBook().getCoverUrl();
        response.addedAt = item.getAddedAt();

        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}
