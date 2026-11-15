package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.RatingRequest;
import com.sds.bookrecommendation.dto.RatingResponse;
import com.sds.bookrecommendation.dto.RatingUpdateRequest;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.exception.BookNotFoundException;
import com.sds.bookrecommendation.exception.DuplicateRatingException;
import com.sds.bookrecommendation.exception.RatingNotFoundException;
import com.sds.bookrecommendation.exception.UserNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business rules for ratings: create, update, delete and list.
 * After every change the book's average rating and rating count are recomputed.
 */
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository,
                         BookRepository bookRepository,
                         UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Saves a new rating. Throws 404 for an unknown user or book,
     * and 409 if the user already rated this book.
     */
    @Transactional
    public RatingResponse rateBook(RatingRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));

        if (ratingRepository.findByUserIdAndBookId(user.getId(), book.getId()).isPresent()) {
            throw new DuplicateRatingException(user.getId(), book.getId());
        }

        Rating saved = ratingRepository.save(new Rating(user, book, request.score()));
        refreshBookStats(book);
        return RatingResponse.from(saved);
    }

    /** Changes the score of an existing rating and updates the book's average. */
    @Transactional
    public RatingResponse updateRating(Long ratingId, RatingUpdateRequest request) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RatingNotFoundException(ratingId));
        rating.setScore(request.score());
        Rating saved = ratingRepository.save(rating);
        refreshBookStats(saved.getBook());
        return RatingResponse.from(saved);
    }

    /** Deletes a rating and updates the book's average and count. */
    @Transactional
    public void deleteRating(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RatingNotFoundException(ratingId));
        Book book = rating.getBook();
        ratingRepository.delete(rating);
        refreshBookStats(book);
    }

    /** Returns one page of a user's ratings. Throws 404 if the user does not exist. */
    @Transactional(readOnly = true)
    public Page<RatingResponse> getRatingsByUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return ratingRepository.findByUserId(userId, pageable).map(RatingResponse::from);
    }

    /** Recomputes the book's average rating (rounded to 2 decimals) and rating count. */
    private void refreshBookStats(Book book) {
        double average = ratingRepository.averageScoreByBookId(book.getId());
        book.setAvgRating(Math.round(average * 100) / 100.0);
        book.setRatingCount((int) ratingRepository.countByBookId(book.getId()));
        bookRepository.save(book);
    }
}