package com.sds.bookrecommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * The answers a user gave in a quiz. Deleted automatically when the user account is deleted.
 */
@Entity
@Table(name = "quiz_responses")
public class QuizResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "quiz_type", nullable = false)
    private String quizType;

    @Column(nullable = false, length = 4000)
    private String answers;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Empty constructor required by JPA. */
    public QuizResponse() {
    }

    /** Creates a stored quiz response; the time is set to now. */
    public QuizResponse(User user, String quizType, String answers) {
        this.user = user;
        this.quizType = quizType;
        this.answers = answers;
    }

    public Long getId() { return id; }

    public User getUser() { return user; }

    public String getQuizType() { return quizType; }

    public String getAnswers() { return answers; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}