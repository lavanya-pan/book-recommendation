package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.BookRequest;
import com.sds.bookrecommendation.dto.BookResponse;
import com.sds.bookrecommendation.dto.PageResponse;
import com.sds.bookrecommendation.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for the book catalogue.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    /** Receives the book service through the constructor. */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Lists books one page at a time. Optional filters: query (title, author or genre),
     * genre and minRating. Sort looks like "title,asc" or "avgRating,desc".
     */
    @GetMapping
    public PageResponse<BookResponse> listBooks(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title,asc") String sort) {
        boolean noFilter = (query == null || query.isBlank())
                && (genre == null || genre.isBlank())
                && minRating <= 0;
        if (noFilter) {
            return bookService.getAllBooks(page, size, sort);
        }
        return bookService.searchBooks(query, genre, minRating, page, size, sort);
    }

    /** Returns every genre in the catalogue, for the genre filter and the Home genre chips. */
    @GetMapping("/genres")
    public List<String> listGenres() {
        return bookService.getGenres();
    }

    /** Returns one book by ID, or 404 if it does not exist. */
    @GetMapping("/{id}")
    public BookResponse getBook(@PathVariable Long id) {
        return bookService.getBook(id);
    }

    /** Creates a book from a validated request and answers 201 Created. */
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(request));
    }

    /** Replaces a book's editable fields, or answers 404 if it does not exist. */
    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return bookService.updateBook(id, request);
    }

    /** Deletes a book and answers 204 No Content, or 404 if it does not exist. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}