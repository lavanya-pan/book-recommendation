
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.ReadingHistoryRequest;
import com.sds.bookrecommendation.dto.ReadingHistoryResponse;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.ReadingHistory;
import com.sds.bookrecommendation.entity.ReadingStatus;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.ReadingHistoryRepository;
import com.sds.bookrecommendation.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingHistoryServiceTest {

    @Mock
    private ReadingHistoryRepository readingHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    private ReadingHistoryService service;
    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        service = new ReadingHistoryService(
                readingHistoryRepository,
                userRepository,
                bookRepository
        );

        user = new User("Test User", "test@example.com");
        user.setId(1L);

        book = new Book();
        book.setId(1096L);
        book.setTitle("The Hunger Games");
        book.setAuthor("Suzanne Collins");
        book.setGenre("Fantasy");
    }

    private ReadingHistoryRequest createRequest(ReadingStatus status) {
        ReadingHistoryRequest request = new ReadingHistoryRequest();
        request.setUserId(1L);
        request.setBookId(1096L);
        request.setStatus(status);
        return request;
    }

    @Test
    void addOrUpdate_newEntry_savesReadingHistory() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1096L)).thenReturn(Optional.of(book));
        when(readingHistoryRepository.findByUser_IdAndBook_Id(1L, 1096L))
                .thenReturn(Optional.empty());
        when(readingHistoryRepository.save(any(ReadingHistory.class)))
                .thenAnswer(call -> call.getArgument(0));

        ReadingHistoryResponse response =
                service.addOrUpdate(createRequest(ReadingStatus.READING));

        assertEquals(1L, response.getUserId());
        assertEquals(1096L, response.getBookId());
        assertEquals("The Hunger Games", response.getBookTitle());
        assertEquals(ReadingStatus.READING, response.getStatus());

        verify(readingHistoryRepository).save(any(ReadingHistory.class));
    }

    @Test
    void addOrUpdate_existingEntry_updatesStatus() {
        ReadingHistory history =
                new ReadingHistory(user, book, ReadingStatus.READING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(1096L)).thenReturn(Optional.of(book));
        when(readingHistoryRepository.findByUser_IdAndBook_Id(1L, 1096L))
                .thenReturn(Optional.of(history));
        when(readingHistoryRepository.save(any(ReadingHistory.class)))
                .thenAnswer(call -> call.getArgument(0));

        ReadingHistoryResponse response =
                service.addOrUpdate(createRequest(ReadingStatus.FINISHED));

        assertEquals(ReadingStatus.FINISHED, response.getStatus());
        assertNotNull(response.getFinishedAt());
        verify(readingHistoryRepository).save(history);
    }

    @Test
    void addOrUpdate_unknownUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ReadingHistoryRequest request = createRequest(ReadingStatus.READING);
        request.setUserId(99L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addOrUpdate(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(bookRepository, never()).findById(any());
    }

    @Test
    void addOrUpdate_unknownBook_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(9999L)).thenReturn(Optional.empty());

        ReadingHistoryRequest request = createRequest(ReadingStatus.READING);
        request.setBookId(9999L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.addOrUpdate(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(readingHistoryRepository, never()).save(any());
    }

    @Test
    void getHistory_validUser_returnsHistory() {
        ReadingHistory history =
                new ReadingHistory(user, book, ReadingStatus.READING);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(readingHistoryRepository.findAllByUser_IdOrderByUpdatedAtDesc(1L))
                .thenReturn(List.of(history));

        List<ReadingHistoryResponse> result = service.getHistory(1L);

        assertEquals(1, result.size());
        assertEquals("The Hunger Games", result.get(0).getBookTitle());
        assertEquals(ReadingStatus.READING, result.get(0).getStatus());
    }

    @Test
    void getHistory_unknownUser_throwsNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getHistory(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(readingHistoryRepository, never())
                .findAllByUser_IdOrderByUpdatedAtDesc(any());
    }

    @Test
    void updateStatus_validEntry_updatesToFinished() {
        ReadingHistory history =
                new ReadingHistory(user, book, ReadingStatus.READING);

        when(readingHistoryRepository.findByUser_IdAndBook_Id(1L, 1096L))
                .thenReturn(Optional.of(history));
        when(readingHistoryRepository.save(any(ReadingHistory.class)))
                .thenAnswer(call -> call.getArgument(0));

        ReadingHistoryResponse response =
                service.updateStatus(1L, 1096L, ReadingStatus.FINISHED);

        assertEquals(ReadingStatus.FINISHED, response.getStatus());
        assertNotNull(response.getFinishedAt());
        verify(readingHistoryRepository).save(history);
    }

    @Test
    void updateStatus_nullStatus_throwsBadRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(1L, 1096L, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(readingHistoryRepository, never()).save(any());
    }

    @Test
    void updateStatus_unknownEntry_throwsNotFound() {
        when(readingHistoryRepository.findByUser_IdAndBook_Id(1L, 9999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(1L, 9999L, ReadingStatus.FINISHED)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}

