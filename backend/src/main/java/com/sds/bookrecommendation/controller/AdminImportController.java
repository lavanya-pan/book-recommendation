package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.ImportSummary;
import com.sds.bookrecommendation.service.DataImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only endpoints that load data into the database.
 */
@RestController
@RequestMapping("/api/admin/import")
public class AdminImportController {

    private final DataImportService dataImportService;

    /** Receives the import service through the constructor. */
    public AdminImportController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    /** Loads the Goodbooks-10k books into the database and returns a summary. Safe to call twice. */
    @PostMapping("/books")
    public ImportSummary importBooks() {
        return dataImportService.importBooks();
    }
}