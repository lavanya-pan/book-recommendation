package com.sds.bookrecommendation.dto;

import com.sds.bookrecommendation.entity.ReadingStatus;
import jakarta.validation.constraints.NotNull;

public class ReadingStatusUpdateRequest {

    @NotNull
    private ReadingStatus status;

    public ReadingStatusUpdateRequest() {
    }

    public ReadingStatus getStatus() {
        return status;
    }

    public void setStatus(ReadingStatus status) {
        this.status = status;
    }
}