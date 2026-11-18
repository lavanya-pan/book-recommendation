package com.sds.bookrecommendation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "reading_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reading_history_user_book",
                        columnNames = {"user_id", "book_id"}
                )
        }
)
public class ReadingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime updatedAt;

    public ReadingHistory() {
    }

    public ReadingHistory(User user, Book book, ReadingStatus status) {
        this.user = user;
        this.book = book;
        updateStatus(status);
    }

    @PrePersist
    public void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateStatus(ReadingStatus status) {
        this.status = status;

        if (status == ReadingStatus.FINISHED) {
            if (finishedAt == null) {
                finishedAt = LocalDateTime.now();
            }
        } else {
            finishedAt = null;
        }

        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Book getBook() {
        return book;
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