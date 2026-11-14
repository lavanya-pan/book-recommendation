package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenreRecommendationServiceTest {

    private BookRepository bookRepository;
    private GenreRecommendationService service;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        service = new GenreRecommendationService(bookRepository);
    }

    @Test
    void takesBooksFromEachGenreInTurn() {
        stub("romance", book(1, "Romance One", "romance"), book(2, "Romance Two", "romance"));
        stub("humor", book(3, "Humor One", "humor"));

        List<RecommendedBook> result = service.byGenres(List.of("romance", "humor"), 3);

        assertEquals(List.of(1L, 3L, 2L), result.stream().map(RecommendedBook::id).toList());
        assertTrue(result.get(0).reason().contains("romance"));
    }

    @Test
    void skipsBoxSetsAndCollections() {
        stub("fantasy", book(1, "Harry Potter Boxed Set", "fantasy"),
                book(2, "A Calvin and Hobbes Collection", "fantasy"),
                book(3, "The Hobbit", "fantasy"));

        List<RecommendedBook> result = service.byGenres(List.of("fantasy"), 5);

        assertEquals(1, result.size());
        assertEquals("The Hobbit", result.get(0).title());
    }

    @Test
    void respectsTheLimit() {
        stub("romance", book(1, "One", "romance"), book(2, "Two", "romance"), book(3, "Three", "romance"));

        assertEquals(2, service.byGenres(List.of("romance"), 2).size());
    }

    @Test
    void ignoresCaseSpacesAndDuplicatesInGenreNames() {
        stub("romance", book(1, "One", "romance"));

        List<RecommendedBook> result = service.byGenres(List.of("  Romance ", "ROMANCE"), 5);

        assertEquals(1, result.size());
        verify(bookRepository).findByGenreIgnoreCaseAndRatingCountGreaterThanEqualOrderByAvgRatingDescRatingCountDesc(
                eq("romance"), eq(GenreRecommendationService.MIN_STORED_RATINGS), any(Pageable.class));
    }

    @Test
    void rejectsAnEmptyGenreList() {
        assertThrows(IllegalArgumentException.class, () -> service.byGenres(List.of(), 5));
        assertThrows(IllegalArgumentException.class, () -> service.byGenres(null, 5));
        assertThrows(IllegalArgumentException.class, () -> service.byGenres(List.of(" "), 5));
    }

    @Test
    void rejectsALimitBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> service.byGenres(List.of("romance"), 0));
    }

    private void stub(String genre, Book... books) {
        when(bookRepository.findByGenreIgnoreCaseAndRatingCountGreaterThanEqualOrderByAvgRatingDescRatingCountDesc(
                eq(genre), eq(GenreRecommendationService.MIN_STORED_RATINGS), any(Pageable.class)))
                .thenReturn(List.of(books));
    }

    private static Book book(long id, String title, String genre) {
        Book book = new Book(title, "Author", genre, "A book", 2000);
        book.setId(id);
        book.setAvgRating(4.5);
        book.setRatingCount(5000);
        return book;
    }
}