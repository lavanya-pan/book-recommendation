
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public RatingService(
            RatingRepository ratingRepository,
            BookRepository bookRepository,
            UserRepository userRepository) {

        this.ratingRepository = ratingRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    // Create a rating for the currently authenticated user
    @Transactional
    public RatingResponse rateBook(
            Long currentUserId,
            RatingRequest request) {

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new UserNotFoundException(currentUserId));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() ->
                        new BookNotFoundException(request.bookId()));

        if (ratingRepository.findByUserIdAndBookId(
                currentUserId, book.getId()).isPresent()) {

            throw new DuplicateRatingException(
                    currentUserId, book.getId());
        }

        Rating rating = new Rating(
                user,
                book,
                request.score()
        );

        Rating saved = ratingRepository.save(rating);

        refreshBookStats(book);

        return RatingResponse.from(saved);
    }

    // Update a rating only if it belongs to the logged-in user
    @Transactional
    public RatingResponse updateRating(
            Long ratingId,
            Long currentUserId,
            RatingUpdateRequest request) {

        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() ->
                        new RatingNotFoundException(ratingId));

        checkOwnership(rating, currentUserId);

        rating.setScore(request.score());

        Rating saved = ratingRepository.save(rating);

        refreshBookStats(saved.getBook());

        return RatingResponse.from(saved);
    }

    // Delete a rating only if it belongs to the logged-in user
    @Transactional
    public void deleteRating(
            Long ratingId,
            Long currentUserId) {

        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() ->
                        new RatingNotFoundException(ratingId));

        checkOwnership(rating, currentUserId);

        Book book = rating.getBook();

        ratingRepository.delete(rating);

        refreshBookStats(book);
    }

    // Check whether the rating belongs to the authenticated user
    private void checkOwnership(
            Rating rating,
            Long currentUserId) {

        if (rating.getUser() == null ||
                !currentUserId.equals(rating.getUser().getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only modify your own ratings"
            );
        }
    }

    // Get ratings belonging to a particular user
    @Transactional(readOnly = true)
    public Page<RatingResponse> getRatingsByUser(
            Long userId,
            Pageable pageable) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return ratingRepository
                .findByUserId(userId, pageable)
                .map(RatingResponse::from);
    }

    // Recalculate book average rating and rating count
    private void refreshBookStats(Book book) {

        double average =
                ratingRepository.averageScoreByBookId(book.getId());

        long count =
                ratingRepository.countByBookId(book.getId());

        book.setAvgRating(
                Math.round(average * 100) / 100.0
        );

        book.setRatingCount((int) count);

        bookRepository.save(book);
    }
}
