
package com.sds.bookrecommendation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "reading_history",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "book_id"}
        )
)
public class ReadingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingStatus status;

    private LocalDateTime finishedAt;

    public ReadingHistory() {
    }

    public ReadingHistory(User user, Book book, ReadingStatus status) {
        this.user = user;
        this.book = book;
        this.status = status;
        if (status == ReadingStatus.FINISHED) {
            this.finishedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public ReadingStatus getStatus() {
        return status;
    }

    public void setStatus(ReadingStatus status) {
        this.status = status;
        if (status == ReadingStatus.FINISHED && this.finishedAt == null) {
            this.finishedAt = LocalDateTime.now();
        }
        if (status != ReadingStatus.FINISHED) {
            this.finishedAt = null;
        }
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public enum ReadingStatus {
        READING,
        FINISHED,
        DROPPED
    }
}
