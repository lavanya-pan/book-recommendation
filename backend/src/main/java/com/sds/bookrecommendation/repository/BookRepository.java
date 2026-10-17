package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.Book;
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
}
