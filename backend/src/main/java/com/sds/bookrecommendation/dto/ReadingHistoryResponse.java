package com.sds.bookrecommendation.dto;

import com.sds.bookrecommendation.entity.ReadingHistory;
import com.sds.bookrecommendation.entity.ReadingStatus;

import java.time.LocalDateTime;

public class ReadingHistoryResponse {

    private Long id;
    private Long userId;
    private Long bookId;
    private String bookTitle;
    private String author;
    private String genre;
    private ReadingStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime updatedAt;

    public static ReadingHistoryResponse from(ReadingHistory history) {
        ReadingHistoryResponse response = new ReadingHistoryResponse();

        response.id = history.getId();
        response.userId = history.getUser().getId();
        response.bookId = history.getBook().getId();
        response.bookTitle = history.getBook().getTitle();
        response.author = history.getBook().getAuthor();
        response.genre = history.getBook().getGenre();
        response.status = history.getStatus();
        response.startedAt = history.getStartedAt();
        response.finishedAt = history.getFinishedAt();
        response.updatedAt = history.getUpdatedAt();

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

    public ReadingStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}