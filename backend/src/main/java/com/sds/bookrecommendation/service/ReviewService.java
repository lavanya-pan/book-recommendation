
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.ReviewRequest;
import com.sds.bookrecommendation.dto.ReviewResponse;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Review;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.exception.BookNotFoundException;
import com.sds.bookrecommendation.exception.UserNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.ReviewRepository;
import com.sds.bookrecommendation.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    // Saves a new review written by the given user for a book.
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new UserNotFoundException(request.userId()));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() ->
                        new BookNotFoundException(request.bookId()));

        Review review = new Review(user, book, request.text().trim());

        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.from(savedReview);
    }

    // Returns all reviews of a book.
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByBook(Long bookId) {

        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        return reviewRepository
                .findByBook_IdOrderByCreatedAtDesc(bookId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }
}
