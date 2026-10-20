package com.sds.bookrecommendation.recommendation;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.exception.BookNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Recommends books whose text (title, author, genre, description) is similar to
 * the books the user liked, using TF-IDF vectors and cosine similarity.
 * The index is built once and rebuilt only when the number of books changes.
 */
@Component
public class ContentBasedStrategy implements RecommendationStrategy {

    /** A rating at or above this value means the user liked the book. */
    static final int LIKED_SCORE = 4;

    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;

    private TfIdfIndex index;
    private long indexedBookCount = -1;

    public ContentBasedStrategy(RatingRepository ratingRepository, BookRepository bookRepository) {
        this.ratingRepository = ratingRepository;
        this.bookRepository = bookRepository;
    }

    /** Name used in the request parameter. */
    @Override
    public String type() {
        return "content";
    }

    /**
     * Returns up to {@code limit} unrated books similar to the books the user liked.
     * Returns an empty list when the user has not liked any book yet,
     * so the service can fall back to another strategy (cold start).
     */
    @Override
    @Transactional(readOnly = true)
    public List<RecommendedBook> recommend(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return List.of();
        }

        // Liked books become the taste profile, weighted by the rating given.
        Set<Long> alreadyRated = new HashSet<>();
        Map<Long, Double> liked = new HashMap<>();
        String bestTitle = null;
        int bestScore = 0;
        for (Rating rating : ratingRepository.findByUserId(userId)) {
            Book book = rating.getBook();
            alreadyRated.add(book.getId());
            if (rating.getScore() >= LIKED_SCORE) {
                liked.put(book.getId(), (double) rating.getScore());
                if (rating.getScore() > bestScore) {
                    bestScore = rating.getScore();
                    bestTitle = book.getTitle();
                }
            }
        }
        if (liked.isEmpty()) {
            return List.of();
        }

        TfIdfIndex current = index();
        List<TfIdfIndex.Match> matches =
                current.findSimilar(current.profile(liked), alreadyRated, limit);
        return toRecommendations(matches, "Similar to books you liked, such as \"" + bestTitle + "\"");
    }

    /**
     * Books similar to one given book, for the "similar books" section of a book page.
     *
     * @throws BookNotFoundException if the book does not exist
     */
    @Transactional(readOnly = true)
    public List<RecommendedBook> similarBooks(long bookId, int limit) {
        String title = bookRepository.findById(bookId)
                .map(Book::getTitle)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        return toRecommendations(index().similarTo(bookId, limit), "Similar to \"" + title + "\"");
    }

    /** Returns the TF-IDF index, building it again if books were added or removed. */
    private synchronized TfIdfIndex index() {
        long count = bookRepository.count();
        if (index == null || count != indexedBookCount) {
            Map<Long, String> documents = new HashMap<>();
            for (Book book : bookRepository.findAll()) {
                documents.put(book.getId(), describe(book));
            }
            index = new TfIdfIndex(documents);
            indexedBookCount = count;
        }
        return index;
    }

    /** The text used to compare books. Genre is repeated so it counts for more. */
    private static String describe(Book book) {
        return String.join(" ", orEmpty(book.getTitle()), orEmpty(book.getAuthor()),
                orEmpty(book.getGenre()), orEmpty(book.getGenre()), orEmpty(book.getDescription()));
    }

    private static String orEmpty(String text) {
        return text == null ? "" : text;
    }

    /** Turns similarity matches into recommendations, keeping the ranked order. */
    private List<RecommendedBook> toRecommendations(List<TfIdfIndex.Match> matches, String reason) {
        List<Long> ids = matches.stream().map(TfIdfIndex.Match::bookId).toList();
        Map<Long, Book> booksById = bookRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        List<RecommendedBook> result = new ArrayList<>();
        for (TfIdfIndex.Match match : matches) {
            Book book = booksById.get(match.bookId());
            if (book != null) {
                double similarity = Math.round(match.similarity() * 1000) / 1000.0;
                result.add(new RecommendedBook(book.getId(), book.getTitle(), book.getAuthor(),
                        book.getGenre(), similarity, reason));
            }
        }
        return result;
    }
}