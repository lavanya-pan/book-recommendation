
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.ImportSummary;
import com.sds.bookrecommendation.service.DataImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataImportService dataImportService;

    @Test
    @WithMockUser(username = "normalUser", roles = "USER")
    void normalUser_cannotImportBooks() throws Exception {

        mockMvc.perform(post("/api/admin/import/books"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dataImportService);
    }

    @Test
    @WithMockUser(username = "adminUser", roles = "ADMIN")
    void admin_canImportBooks() throws Exception {

        when(dataImportService.importBooks())
                .thenReturn(new ImportSummary(
                        10, 10, 0, 0, 0, 0
                ));

        mockMvc.perform(post("/api/admin/import/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(10))
                .andExpect(jsonPath("$.imported").value(10))
                .andExpect(jsonPath("$.skippedBlankTitle").value(0));

        verify(dataImportService, times(1)).importBooks();
    }
}
