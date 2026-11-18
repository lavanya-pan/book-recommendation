package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.MoodQuizRequest;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.quiz.MoodPreferences;
import com.sds.bookrecommendation.recommendation.RecommendedBook;
import com.sds.bookrecommendation.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests how mood quiz answers become preferences and how books are filtered and ranked.
 */
class MoodQuizServiceTest {

    private BookRepository bookRepository;
    private MoodQuizService service;

    /** Creates the service with a fake book repository that returns no books by default. */
    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        service = new MoodQuizService(bookRepository);
    }

    /** Builds a request with the given mood, length and romance answer. */
    private static MoodQuizRequest request(String mood, String length, boolean romance) {
        return new MoodQuizRequest(mood, "scifi", length, romance, "mysterious");
    }

    /** Builds a book with the given values. */
    private static Book book(long id, String genre, Integer pages, double rating, String tags) {
        Book book = new Book();
        book.setId(id);
        book.setTitle("Book " + id);
        book.setAuthor("Author");
        book.setGenre(genre);
        book.setPageCount(pages);
        book.setAvgRating(rating);
        book.setTags(tags);
        return book;
    }

    /** Short, medium and long give the expected page ranges. */
    @Test
    void lengthAnswersGivePageRanges() {
        MoodPreferences shortPrefs = service.moodToPreferences(request("cozy", "short", false));
        MoodPreferences longPrefs = service.moodToPreferences(request("cozy", "long", false));

        assertEquals(0, shortPrefs.minPages());
        assertEquals(250, shortPrefs.maxPages());
        assertEquals(500, longPrefs.minPages());
        assertEquals(Integer.MAX_VALUE, longPrefs.maxPages());
    }

    /** Mood and world genres are combined, and romance is added when the answer is yes. */
    @Test
    void genresCombineMoodWorldAndRomance() {
        MoodPreferences prefs = service.moodToPreferences(request("thrilling", "medium", true));

        assertTrue(prefs.genreWeights().containsKey("thriller"));
        assertTrue(prefs.genreWeights().containsKey("sci-fi"));
        assertTrue(prefs.genreWeights().containsKey("romance"));
        assertTrue(prefs.romance());
    }

    /** Books outside the page range are dropped; books with unknown pages stay. */
    @Test
    void booksOutsidePageRangeAreDropped() {
        when(bookRepository.findByGenreIgnoreCase("thriller")).thenReturn(List.of(
                book(1, "thriller", 300, 4, ""),
                book(2, "thriller", 800, 4, ""),
                book(3, "thriller", null, 4, "")));

        List<RecommendedBook> result = service.recommend(request("thrilling", "medium", false), 10);

        List<Long> ids = result.stream().map(RecommendedBook::id).toList();
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(3L));
        assertTrue(!ids.contains(2L));
    }

    /** With romance = no, romance books are left out. */
    @Test
    void romanceNoExcludesRomanceBooks() {
        when(bookRepository.findByGenreIgnoreCase("romance"))
                .thenReturn(List.of(book(1, "romance", 300, 5, "")));
        when(bookRepository.findByGenreIgnoreCase("contemporary"))
                .thenReturn(List.of(book(2, "contemporary", 300, 3, "")));

        List<RecommendedBook> result = service.recommend(request("cozy", "medium", false), 10);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).id());
    }

    /** A book with matching keywords and a higher rating comes first. */
    @Test
    void bestMatchComesFirst() {
        when(bookRepository.findByGenreIgnoreCase("thriller")).thenReturn(List.of(
                book(1, "thriller", 300, 3, ""),
                book(2, "thriller", 300, 4, "suspense, mystery")));

        List<RecommendedBook> result = service.recommend(request("thrilling", "medium", false), 10);

        assertEquals(2L, result.get(0).id());
        assertEquals("Matches your thrilling mood", result.get(0).reason());
    }

    /** The limit caps the number of results. */
    @Test
    void limitCapsResults() {
        when(bookRepository.findByGenreIgnoreCase("thriller")).thenReturn(List.of(
                book(1, "thriller", 300, 4, ""),
                book(2, "thriller", 300, 3, ""),
                book(3, "thriller", 300, 2, "")));

        assertEquals(2, service.recommend(request("thrilling", "medium", false), 2).size());
    }

    /** Unknown answers are rejected with a message listing valid values. */
    @Test
    void unknownAnswersAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.moodToPreferences(request("sleepy", "medium", false)));
        assertThrows(IllegalArgumentException.class,
                () -> service.moodToPreferences(request("cozy", "tiny", false)));
    }
}