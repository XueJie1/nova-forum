package com.novaforum.nova_forum.config;

import com.novaforum.nova_forum.controller.SearchController;
import com.novaforum.nova_forum.dto.SearchResponse;
import com.novaforum.nova_forum.service.SearchService;
import com.novaforum.nova_forum.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@ContextConfiguration(classes = {
        SearchController.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void allowsAnonymousReadOnlySearchEndpoints() throws Exception {
        when(searchService.searchPosts(any())).thenReturn(new SearchResponse());
        when(searchService.getSearchSuggestions("java", 5)).thenReturn(List.of("java 17"));

        mockMvc.perform(get("/search/posts").param("keyword", "java"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/search/suggestions").param("keyword", "java"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsAnonymousIndexManagementRequests() throws Exception {
        mockMvc.perform(post("/search/index/create"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/search/index/rebuild"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/search/index/status"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/search/index"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(searchService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void rejectsIndexManagementForRegularUsers() throws Exception {
        mockMvc.perform(post("/search/index/rebuild"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(searchService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsIndexManagementForAdministrators() throws Exception {
        when(searchService.indexExists()).thenReturn(true);

        mockMvc.perform(delete("/search/index"))
                .andExpect(status().isOk());

        verify(searchService).deleteIndex();
    }
}
