package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.InteractionRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final InteractionRepository interactionRepository;
    private final BookRepository bookRepository;

    // Initializes UserProfileService with required user, rating, interaction, and book repositories.
    public UserProfileService(
            UserRepository userRepository,
            RatingRepository ratingRepository,
            InteractionRepository interactionRepository,
            BookRepository bookRepository) {

        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
        this.interactionRepository = interactionRepository;
        this.bookRepository = bookRepository;
    }

    // Retrieves a user profile by ID or throws 404 if not found.
    @Transactional(readOnly = true)
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // Updates and saves the user's preferred UI theme in uppercase.
    @Transactional
    public User updateTheme(Long userId, String theme) {
        User user = getProfile(userId);
        user.setTheme(theme.toUpperCase());
        return userRepository.save(user);
    }

    // Updates and saves the user's reader type preference.
    @Transactional
    public User updateReaderType(Long userId, String readerType) {
        User user = getProfile(userId);
        user.setReaderType(readerType);
        return userRepository.save(user);
    }

    // Updates user name and email while throwing 409 Conflict if the email is already taken.
    @Transactional
    public User updateProfile(Long userId, String name, String email) {

        User user = getProfile(userId);

        String updatedName = name.trim();
        String updatedEmail = email.trim();

        // Check whether another account already uses this email.
        userRepository.findByEmail(updatedEmail)
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email is already registered"
                    );
                });

        user.setName(updatedName);
        user.setEmail(updatedEmail);

        return userRepository.save(user);
    }

    // Deletes a user account, clears interactions, and recalculates affected book rating statistics.
    @Transactional
    public void deleteAccount(Long userId) {

        User user = getProfile(userId);

        List<Rating> userRatings =
                ratingRepository.findByUserId(userId);

        Set<Long> affectedBookIds = new HashSet<>();

        for (Rating rating : userRatings) {
            affectedBookIds.add(rating.getBook().getId());
        }

        // Delete this user's interactions.
        interactionRepository.deleteByUserId(userId);

        // Delete the user and their ratings through the existing cascade.
        userRepository.delete(user);
        userRepository.flush();

        // Recalculate rating statistics for affected books.
        for (Long bookId : affectedBookIds) {

            bookRepository.findById(bookId).ifPresent(book -> {

                double average =
                        ratingRepository.averageScoreByBookId(bookId);

                long count =
                        ratingRepository.countByBookId(bookId);

                book.setAvgRating(average);
                book.setRatingCount((int) count);

                bookRepository.save(book);
            });
        }
    }
}