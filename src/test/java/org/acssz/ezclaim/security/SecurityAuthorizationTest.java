package org.acssz.ezclaim.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.acssz.ezclaim.domain.Claim;
import org.acssz.ezclaim.domain.ClaimStatus;
import org.acssz.ezclaim.domain.Tag;
import org.acssz.ezclaim.service.AuditEventService;
import org.acssz.ezclaim.service.ClaimService;
import org.acssz.ezclaim.service.PhotoService;
import org.acssz.ezclaim.service.TagService;
import org.acssz.ezclaim.web.AuditEventController;
import org.acssz.ezclaim.web.ClaimController;
import org.acssz.ezclaim.web.PhotoController;
import org.acssz.ezclaim.web.TagController;
import org.acssz.ezclaim.security.SecurityRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@WebMvcTest(controllers = {
        ClaimController.class, TagController.class, PhotoController.class, AuditEventController.class
})
@Import({SecurityAuthorizationTest.MockBeans.class, SecurityAuthorizationTest.TestSecurity.class})
class SecurityAuthorizationTest {

    @Autowired MockMvc mvc;
    @Autowired ClaimService claimService;
    @Autowired TagService tagService;
    @Autowired PhotoService photoService;
    @Autowired AuditEventService auditEventService;

    @BeforeEach
    void setupStubs() {
        Claim c1 = Claim.builder().id("c1").title("t1").status(ClaimStatus.NEW)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(claimService.findById("c1")).thenReturn(c1);
        when(claimService.findAll()).thenReturn(List.of(c1));

        when(tagService.list()).thenReturn(List.of(Tag.builder().id("t1").label("L").color("#fff").build()));

        when(photoService.list()).thenReturn(List.of());

        when(auditEventService.search(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
    }

    @Test
    void anonymous_cannot_list_claims() throws Exception {
        mvc.perform(get("/api/claims")).andExpect(status().isForbidden());
    }

    @Test
    void anonymous_can_get_single_claim() throws Exception {
        mvc.perform(get("/api/claims/c1")).andExpect(status().isOk());
    }

    @Test
    void reader_can_list_claims() throws Exception {
        mvc.perform(get("/api/claims").with(jwt().jwt(j -> j.claim("scope", "CLAIM_READ"))))
                .andExpect(status().isOk());
    }

    @Test
    void reader_cannot_create_tag_admin_can() throws Exception {
        // Reader only has TAG_READ
        mvc.perform(post("/api/tags")
                        .with(jwt().jwt(j -> j.claim("scope", "TAG_READ")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"a\",\"color\":\"#fff\"}"))
                .andExpect(status().isForbidden());

        // Admin has TAG_WRITE
        when(tagService.create(eq("a"), eq("#fff"))).thenReturn(Tag.builder().id("t1").label("a").color("#fff").build());
        mvc.perform(post("/api/tags")
                        .with(jwt().jwt(j -> j.claim("scope", "TAG_WRITE TAG_READ")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"a\",\"color\":\"#fff\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void audit_requires_scope() throws Exception {
        mvc.perform(get("/api/audit-events"))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/audit-events").with(jwt().jwt(j -> j.claim("scope", "AUDIT"))))
                .andExpect(status().isOk());
    }

    @Test
    void photos_read_and_delete_authorization() throws Exception {
        // list requires PHOTO_READ
        mvc.perform(get("/api/photos")).andExpect(status().isForbidden());
        mvc.perform(get("/api/photos").with(jwt().jwt(j -> j.claim("scope", "PHOTO_READ"))))
                .andExpect(status().isOk());

        // delete requires PHOTO_DELETE
        mvc.perform(delete("/api/photos/p1").with(jwt().jwt(j -> j.claim("scope", "PHOTO_READ"))))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/photos/p1").with(jwt().jwt(j -> j.claim("scope", "PHOTO_DELETE"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void claims_update_requires_write_scope() throws Exception {
        when(claimService.update(eq("c1"), eq("new"), eq("nd"), eq(ClaimStatus.SUBMITTED), any(), any()))
                .thenReturn(Claim.builder().id("c1").title("new").status(ClaimStatus.SUBMITTED)
                        .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        // reader with only CLAIM_READ should be forbidden
        mvc.perform(put("/api/claims/c1")
                        .with(jwt().jwt(j -> j.claim("scope", "CLAIM_READ")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"new\",\"description\":\"nd\",\"status\":\"SUBMITTED\"}"))
                .andExpect(status().isForbidden());

        // admin with CLAIM_WRITE allowed
        mvc.perform(put("/api/claims/c1")
                        .with(jwt().jwt(j -> j.claim("scope", "CLAIM_WRITE CLAIM_READ")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"new\",\"description\":\"nd\",\"status\":\"SUBMITTED\"}"))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class MockBeans {
        @Bean ClaimService claimService() { return Mockito.mock(ClaimService.class); }
        @Bean TagService tagService() { return Mockito.mock(TagService.class); }
        @Bean PhotoService photoService() { return Mockito.mock(PhotoService.class); }
        @Bean AuditEventService auditEventService() { return Mockito.mock(AuditEventService.class); }
    }

    @TestConfiguration
    static class TestSecurity {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(SecurityRules::apply);
            return http.build();
        }
    }
}
