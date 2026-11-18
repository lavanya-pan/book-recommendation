package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.BookRequest;
import com.sds.bookrecommendation.dto.BookResponse;
import com.sds.bookrecommendation.dto.PageResponse;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.exception.BookNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for books: create, read, update, delete, list and search.
 */
@Service
@Transactional(readOnly = true)
public class BookService {

    /** The largest page a client may ask for. Bigger requests are cut to this size. */
    static final int MAX_PAGE_SIZE = 100;

    /** Book fields a client may sort by. */
    static final List<String> SORT_FIELDS = List.of(
            "id", "title", "author", "genre", "publishedYear", "avgRating", "ratingCount");

    private final BookRepository bookRepository;

    /** Receives the repository through the constructor. */
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /** Creates a new book from the request and returns it. The rating fields start at zero. */
    @Transactional
    public BookResponse addBook(BookRequest request) {
        Book book = new Book();
        applyRequest(book, request);
        return BookResponse.from(bookRepository.save(book));
    }

    /** Replaces the editable fields of an existing book. Throws BookNotFoundException for an unknown ID. */
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        applyRequest(book, request);
        return BookResponse.from(bookRepository.save(book));
    }

    /** Deletes a book. Throws BookNotFoundException for an unknown ID. */
    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    /** Returns one book. Throws BookNotFoundException for an unknown ID. */
    public BookResponse getBook(Long id) {
        return bookRepository.findById(id)
                .map(BookResponse::from)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    /** Returns one page of all books, sorted as requested (for example "title,asc"). */
    public PageResponse<BookResponse> getAllBooks(int page, int size, String sort) {
        Page<Book> result = bookRepository.findAll(buildPageable(page, size, sort));
        return PageResponse.from(result.map(BookResponse::from));
    }

    /**
     * Returns one page of books filtered by free text (title, author or genre), exact genre and a
     * minimum average rating. A blank query or genre means "no filter".
     */
    public PageResponse<BookResponse> searchBooks(String query, String genre, double minRating,
                                                  int page, int size, String sort) {
        String q = query == null ? "" : query.trim();
        String g = genre == null ? "" : genre.trim();
        Page<Book> result = bookRepository.searchFiltered(q, g, minRating, buildPageable(page, size, sort));
        return PageResponse.from(result.map(BookResponse::from));
    }

    /** Returns every genre in the catalogue in alphabetical order, for filters and genre chips. */
    public List<String> getGenres() {
        return bookRepository.findAllGenres();
    }

    /** Copies the editable fields from a request onto a book. Rating fields are never touched here. */
    private void applyRequest(Book book, BookRequest request) {
        book.setTitle(request.title().trim());
        book.setAuthor(request.author().trim());
        book.setGenre(request.genre().trim());
        book.setDescription(request.description());
        book.setPublishedYear(request.publishedYear());
        book.setCoverUrl(request.coverUrl());
        book.setPageCount(request.pageCount());
        book.setTags(request.tags());
    }

    /** Checks the paging values and builds a Pageable. Throws IllegalArgumentException (HTTP 400) if invalid. */
    private Pageable buildPageable(int page, int size, String sort) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be 0 or more");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be at least 1");
        }
        return PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), parseSort(sort));
    }

    /**
     * Turns text like "avgRating,desc" into a Sort. Ties are broken by ID so pages never overlap.
     * Throws IllegalArgumentException (HTTP 400) for an unknown field or direction.
     */
    static Sort parseSort(String sort) {
        String[] parts = (sort == null || sort.isBlank() ? "title,asc" : sort).split(",");
        String field = parts[0].trim();
        if (!SORT_FIELDS.contains(field)) {
            throw new IllegalArgumentException("Cannot sort by '" + field + "'. Allowed: " + SORT_FIELDS);
        }
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String text = parts[1].trim();
            if (text.equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            } else if (!text.equalsIgnoreCase("asc")) {
                throw new IllegalArgumentException("Sort direction must be asc or desc");
            }
        }
        Sort result = Sort.by(direction, field);
        return field.equals("id") ? result : result.and(Sort.by(Sort.Direction.ASC, "id"));
    }
}