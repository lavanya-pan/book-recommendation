package com.sds.bookrecommendation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

/**
 * A book in the catalogue. Genre is stored so genre-based recommendations are possible.
 */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String author;

    @NotBlank
    @Column(nullable = false)
    private String genre;

    @Column(length = 2000)
    private String description;

    private Integer publishedYear;

    /** URL of the book cover image. */
    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    /** Number of pages. Optional, because the dataset has no page counts. */
    @Column(name = "page_count")
    private Integer pageCount;

    /** Comma-separated tags, used by the content-based recommender. */
    @Column(name = "tags", length = 1000)
    private String tags;

    /** Average of all user ratings. Updated by the rating module. */
    @Column(name = "avg_rating", nullable = false)
    private double avgRating = 0.0;

    /** How many ratings the book has received. Updated by the rating module. */
    @Column(name = "rating_count", nullable = false)
    private int ratingCount = 0;

    /** All ratings given to this book (inverse side of Rating.book). */
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    public Book() {
    }

    public Book(String title, String author, String genre, String description, Integer publishedYear) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.description = description;
        this.publishedYear = publishedYear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }

    public List<Rating> getRatings() { return ratings; }
    public void setRatings(List<Rating> ratings) { this.ratings = ratings; }
}
