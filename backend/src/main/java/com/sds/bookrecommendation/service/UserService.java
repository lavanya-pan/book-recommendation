
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.repository.WishlistRepository;
import com.sds.bookrecommendation.repository.ReadingHistoryRepository;
import com.sds.bookrecommendation.repository.ReviewRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final ReviewRepository reviewRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UserService(
            UserRepository userRepository,
            WishlistRepository wishlistRepository,
            ReadingHistoryRepository readingHistoryRepository,
            ReviewRepository reviewRepository) {

        this.userRepository = userRepository;
        this.wishlistRepository = wishlistRepository;
        this.readingHistoryRepository = readingHistoryRepository;
        this.reviewRepository = reviewRepository;
    }

    // Register a new user
    public User registerUser(String name, String email, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User(name, email);
        user.setPasswordHash(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    // Get user by ID
    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }

    // Update user's theme
    public User updateTheme(Long id, String theme) {

        User user = getUserById(id);
        user.setTheme(theme);

        return userRepository.save(user);
    }

    // Delete user's related records and account
    public void deleteUser(Long id) {

        User user = getUserById(id);

        wishlistRepository.deleteAll(
                wishlistRepository.findByUserId(id));

        readingHistoryRepository.deleteAll(
                readingHistoryRepository.findByUserId(id));

        reviewRepository.deleteAll(
                reviewRepository.findByUserId(id));

        userRepository.delete(user);
    }
}
