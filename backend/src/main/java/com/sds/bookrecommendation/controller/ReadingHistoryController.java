
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.entity.ReadingHistory;
import com.sds.bookrecommendation.security.CurrentUser;
import com.sds.bookrecommendation.service.ReadingHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-history")
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    public ReadingHistoryController(
            ReadingHistoryService readingHistoryService) {
        this.readingHistoryService = readingHistoryService;
    }

    // Request body for adding a book to reading history
    public record ReadingHistoryRequest(
            Long bookId,
            ReadingHistory.ReadingStatus status) {
    }

    // Add a book to the logged-in user's reading history
    @PostMapping
    public ResponseEntity<ReadingHistory> addToHistory(
            @RequestBody ReadingHistoryRequest request) {

        Long userId = CurrentUser.getId();

        ReadingHistory history =
                readingHistoryService.addToHistory(
                        userId,
                        request.bookId(),
                        request.status());

        return ResponseEntity.ok(history);
    }

    // Get the logged-in user's reading history
    @GetMapping
    public ResponseEntity<List<ReadingHistory>> getReadingHistory() {

        Long userId = CurrentUser.getId();

        return ResponseEntity.ok(
                readingHistoryService.getReadingHistory(userId));
    }

    // Update the reading status of a book
    @PutMapping("/{bookId}")
    public ResponseEntity<ReadingHistory> updateStatus(
            @PathVariable Long bookId,
            @RequestBody StatusRequest request) {

        Long userId = CurrentUser.getId();

        ReadingHistory updated =
                readingHistoryService.updateStatus(
                        userId,
                        bookId,
                        request.status());

        return ResponseEntity.ok(updated);
    }

    // Request body for updating reading status
    public record StatusRequest(
            ReadingHistory.ReadingStatus status) {
    }
}
