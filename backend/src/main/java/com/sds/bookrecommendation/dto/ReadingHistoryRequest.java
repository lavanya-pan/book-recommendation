package com.sds.bookrecommendation.dto;

import com.sds.bookrecommendation.entity.ReadingStatus;
import jakarta.validation.constraints.NotNull;

public class ReadingHistoryRequest {

    // Set by the controller from the logged-in user; any value sent by the client is ignored.
    private Long userId;

    @NotNull
    private Long bookId;

    @NotNull
    private ReadingStatus status;

    public ReadingHistoryRequest() {
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public ReadingStatus getStatus() { return status; }
    public void setStatus(ReadingStatus status) { this.status = status; }
}
