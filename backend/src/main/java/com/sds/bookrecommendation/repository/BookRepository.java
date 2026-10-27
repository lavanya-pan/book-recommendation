package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    /** Case-insensitive search on title, author or genre. */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Book> search(@Param("q") String query);

    List<Book> findByGenreIgnoreCase(String genre);

    /**
     * One page of books filtered by free text (title, author or genre), exact genre
     * (ignoring case) and a minimum average rating. An empty q or genre means "no filter".
     * The service passes "" instead of null, and sorting comes from the Pageable.
     */
    @Query("SELECT b FROM Book b WHERE "
            + "(:q = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :q, '%'))) "
            + "AND (:genre = '' OR LOWER(b.genre) = LOWER(:genre)) "
            + "AND b.avgRating >= :minRating")
    Page<Book> searchFiltered(@Param("q") String query,
                              @Param("genre") String genre,
                              @Param("minRating") double minRating,
                              Pageable pageable);

    /** All distinct genres in alphabetical order, for the genre filter and the Home genre chips. */
    @Query("SELECT DISTINCT b.genre FROM Book b ORDER BY b.genre")
    List<String> findAllGenres();
}