package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.WishlistRequest;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.UserRepository;
import com.sds.bookrecommendation.service.JWTService;
import com.sds.bookrecommendation.service.WishlistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UserRepository userRepository;

    // Logs in a fake user with id 1 before each test.
    @BeforeEach
    void setUp() {
        User principal = new User("Charu", "charu@gmail.com");
        principal.setId(1L);
        principal.setRole("USER");

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        principal, null, java.util.Collections.emptyList()));
    }

    // Clears the fake login after each test.
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // A userId sent in the URL must be ignored; the logged-in user's wishlist is read.
    @Test
    void getWishlist_usesLoggedInUser_ignoresUserIdParameter() throws Exception {
        when(wishlistService.getWishlist(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/wishlist").param("userId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(wishlistService).getWishlist(1L);
        verify(wishlistService, never()).getWishlist(999L);
    }

    // A userId sent in the body must be replaced by the logged-in user's id.
    @Test
    void addToWishlist_usesLoggedInUser_ignoresUserIdInBody() throws Exception {
        mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 999,
                                    "bookId": 5
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<WishlistRequest> captor =
                ArgumentCaptor.forClass(WishlistRequest.class);
        verify(wishlistService).addToWishlist(captor.capture());

        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(5L, captor.getValue().getBookId());
    }

    // The client does not have to send a userId at all.
    @Test
    void addToWishlist_withoutUserId_isAccepted() throws Exception {
        mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "bookId": 5
                                }
                                """))
                .andExpect(status().isCreated());
    }

    // A request without a book id is rejected and nothing is saved.
    @Test
    void addToWishlist_missingBookId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(wishlistService);
    }

    // Removing a book always removes it from the logged-in user's wishlist.
    @Test
    void removeFromWishlist_usesLoggedInUser() throws Exception {
        mockMvc.perform(delete("/api/wishlist/5").param("userId", "999"))
                .andExpect(status().isNoContent());

        verify(wishlistService).removeFromWishlist(1L, 5L);
    }

    // Without a logged-in user the endpoint is refused and the service is never called.
    @Test
    void getWishlist_withoutLogin_isForbidden() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(wishlistService);
    }
}
