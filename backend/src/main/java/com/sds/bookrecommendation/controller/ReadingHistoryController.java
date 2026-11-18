package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.ReadingHistoryRequest;
import com.sds.bookrecommendation.dto.ReadingHistoryResponse;
import com.sds.bookrecommendation.dto.ReadingStatusUpdateRequest;
import com.sds.bookrecommendation.security.CurrentUser;
import com.sds.bookrecommendation.service.ReadingHistoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
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

    // Adds or updates a reading status for the logged-in user; any userId sent by the client is ignored.
    @PostMapping
    public ResponseEntity<ReadingHistoryResponse> addOrUpdate(
            @Valid @RequestBody ReadingHistoryRequest request) {

        request.setUserId(CurrentUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(readingHistoryService.addOrUpdate(request));
    }

    // Returns the logged-in user's own reading history.
    @GetMapping
    public ResponseEntity<List<ReadingHistoryResponse>> getHistory() {

        return ResponseEntity.ok(
                readingHistoryService.getHistory(CurrentUser.getId()));
    }

    // Changes the status of a book in the logged-in user's own reading history.
    @PutMapping("/{bookId}")
    public ResponseEntity<ReadingHistoryResponse> updateStatus(
            @PathVariable Long bookId,
            @Valid @RequestBody ReadingStatusUpdateRequest request) {

        return ResponseEntity.ok(
                readingHistoryService.updateStatus(
                        CurrentUser.getId(), bookId, request.getStatus()));
    }
}
