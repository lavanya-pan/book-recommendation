package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.RatingImportSummary;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Rating;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.RatingRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Loads a sample of the Goodbooks ratings (data/ratings-sample.csv) as ratings of dummy readers.
 * The dataset book id equals the database book id. Book averages are not changed.
 */
@Service
public class RatingImportService {

    static final String RATINGS_FILE = "data/ratings-sample.csv";
    static final String EMAIL_DOMAIN = "@dataset.local";
    static final int DEFAULT_MAX_USERS = 1000;
    static final int BATCH_SIZE = 1000;

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final RatingRepository ratingRepository;
    private final PasswordEncoder passwordEncoder;

    public RatingImportService(UserRepository userRepository,
                               BookRepository bookRepository,
                               RatingRepository ratingRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.ratingRepository = ratingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Imports the ratings of dataset users 1 to maxUsers. A dataset user that already exists
     * is skipped completely, so calling this twice is safe. Returns a summary of the work.
     */
    @Transactional
    public RatingImportSummary importRatings(int maxUsers) {
        int limit = maxUsers <= 0 ? DEFAULT_MAX_USERS : maxUsers;
        Map<Long, List<int[]>> rowsByUser = new TreeMap<>();
        int totalRows = readRows(limit, rowsByUser);

        Set<Long> bookIds = bookRepository.findAll().stream().map(Book::getId).collect(Collectors.toSet());
        String unusableHash = passwordEncoder.encode(UUID.randomUUID().toString());

        int newUsers = 0;
        int skippedUsers = 0;
        int imported = 0;
        int unknownBook = 0;
        int invalidScore = 0;
        int duplicate = 0;
        List<Rating> batch = new ArrayList<>();

        for (Map.Entry<Long, List<int[]>> entry : rowsByUser.entrySet()) {
            String email = "reader" + entry.getKey() + EMAIL_DOMAIN;
            if (userRepository.existsByEmail(email)) {
                skippedUsers++;
                continue;
            }
            User user = new User("Reader " + entry.getKey(), email);
            user.setPasswordHash(unusableHash);
            user = userRepository.save(user);
            newUsers++;

            Set<Long> seen = new HashSet<>();
            for (int[] row : entry.getValue()) {
                long bookId = row[0];
                int score = row[1];
                if (score < 1 || score > 5) {
                    invalidScore++;
                } else if (!bookIds.contains(bookId)) {
                    unknownBook++;
                } else if (!seen.add(bookId)) {
                    duplicate++;
                } else {
                    batch.add(new Rating(user, bookRepository.getReferenceById(bookId), score));
                    if (batch.size() >= BATCH_SIZE) {
                        ratingRepository.saveAll(batch);
                        imported += batch.size();
                        batch.clear();
                    }
                }
            }
        }
        if (!batch.isEmpty()) {
            ratingRepository.saveAll(batch);
            imported += batch.size();
        }
        return new RatingImportSummary(totalRows, newUsers, skippedUsers, imported,
                unknownBook, invalidScore, duplicate);
    }

    /** Reads the CSV and groups [bookId, score] rows by dataset user id, keeping users up to the limit. */
    private int readRows(int limit, Map<Long, List<int[]>> rowsByUser) {
        int total = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(RATINGS_FILE).getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length < 3) {
                    continue;
                }
                long userId;
                int bookId;
                int score;
                try {
                    userId = Long.parseLong(parts[0].trim());
                    bookId = Integer.parseInt(parts[1].trim());
                    score = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (userId > limit) {
                    continue;
                }
                rowsByUser.computeIfAbsent(userId, key -> new ArrayList<>()).add(new int[]{bookId, score});
                total++;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + RATINGS_FILE, e);
        }
        return total;
    }
}