package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.RatingImportSummary;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the ratings importer with fake repositories and the real sample file.
 */
class RatingImportServiceTest {

    private UserRepository userRepository;
    private BookRepository bookRepository;
    private RatingRepository ratingRepository;
    private RatingImportService service;

    /** Creates the service with fake repositories and a fake password encoder. */
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        bookRepository = mock(BookRepository.class);
        ratingRepository = mock(RatingRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        when(bookRepository.getReferenceById(anyLong())).thenAnswer(call -> {
            Book book = new Book();
            book.setId(call.getArgument(0));
            return book;
        });
        service = new RatingImportService(userRepository, bookRepository, ratingRepository, encoder);
    }

    /** Fakes a catalogue that contains books 1 to 10000. */
    private void givenAllBooksExist() {
        List<Book> books = new ArrayList<>();
        for (long id = 1; id <= 10000; id++) {
            Book book = new Book();
            book.setId(id);
            books.add(book);
        }
        when(bookRepository.findAll()).thenReturn(books);
    }

    /** One new user is created and every row of that user is saved when all books exist. */
    @Test
    void importsOneUserWithAllRatings() {
        givenAllBooksExist();

        RatingImportSummary summary = service.importRatings(1);

        assertEquals(1, summary.newUsers());
        assertTrue(summary.imported() > 0);
        assertEquals(summary.totalRows(),
                summary.imported() + summary.skippedDuplicate() + summary.skippedInvalidScore());
        verify(ratingRepository).saveAll(anyIterable());
    }

    /** A dataset user who already exists is skipped completely. */
    @Test
    void existingUserIsSkipped() {
        givenAllBooksExist();
        when(userRepository.existsByEmail("reader1@dataset.local")).thenReturn(true);

        RatingImportSummary summary = service.importRatings(1);

        assertEquals(0, summary.newUsers());
        assertEquals(1, summary.skippedExistingUsers());
        assertEquals(0, summary.imported());
        verify(ratingRepository, never()).saveAll(anyIterable());
    }

    /** Ratings of books that are not in the catalogue are skipped and counted. */
    @Test
    void unknownBooksAreSkipped() {
        when(bookRepository.findAll()).thenReturn(List.of());

        RatingImportSummary summary = service.importRatings(1);

        assertEquals(0, summary.imported());
        assertEquals(summary.totalRows(), summary.skippedUnknownBook());
    }

    /** More users means more rows, so maxUsers controls the size of the seed. */
    @Test
    void maxUsersControlsHowManyUsersAreCreated() {
        givenAllBooksExist();

        RatingImportSummary small = service.importRatings(2);

        assertEquals(2, small.newUsers());
    }

    /** The imported ratings use the dummy reader as owner and a score from 1 to 5. */
    @Test
    void savedRatingsHaveValidScores() {
        givenAllBooksExist();
        org.mockito.ArgumentCaptor<Iterable<Rating>> captor = org.mockito.ArgumentCaptor.forClass(Iterable.class);

        service.importRatings(1);

        verify(ratingRepository).saveAll(captor.capture());
        for (Rating rating : captor.getValue()) {
            assertTrue(rating.getScore() >= 1 && rating.getScore() <= 5);
        }
    }
}