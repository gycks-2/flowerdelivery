package com.yhc.flower_delivery;

import com.yhc.flower_delivery.domain.account.entity.Account;
import com.yhc.flower_delivery.domain.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SignupIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired AccountRepository accounts;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;

    private String body(String email, String password, String role) {
        return """
                {"email":"%s","password":"%s","role":"%s","name":"테스트","phone":"010-1234-5678","vehicleInfo":"자전거"}
                """.formatted(email, password, role);
    }

    @ParameterizedTest
    @CsvSource({"CUSTOMER,customers", "STORE_OWNER,store_owners", "RIDER,riders"})
    void createsAccountAndSharedIdProfile(String role, String table) throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/auth/signup").contentType("application/json")
                        .content(body(email, "test-password-123!", role)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value(role))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        Account account = accounts.findByEmail(email).orElseThrow();
        assertThat(account.getPasswordHash()).isNotEqualTo("test-password-123!");
        assertThat(encoder.matches("test-password-123!", account.getPasswordHash())).isTrue();
        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getUpdatedAt()).isNotNull();
        assertThat(jdbc.queryForObject("SELECT account_id FROM " + table + " WHERE account_id = ?", Long.class, account.getId()))
                .isEqualTo(account.getId());
        if (role.equals("RIDER")) {
            assertThat(jdbc.queryForObject("SELECT approval_status FROM riders WHERE account_id = ?", String.class, account.getId())).isEqualTo("PENDING");
            assertThat(jdbc.queryForObject("SELECT is_online FROM riders WHERE account_id = ?", Boolean.class, account.getId())).isFalse();
        }
    }

    @Test
    void normalizesEmailAndRejectsDuplicateAcrossRoles() throws Exception {
        String email = UUID.randomUUID() + "@Example.COM";
        mvc.perform(post("/api/auth/signup").contentType("application/json").content(body(email, "password123!", "CUSTOMER")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.email").value(email.toLowerCase(java.util.Locale.ROOT)));
        mvc.perform(post("/api/auth/signup").contentType("application/json").content(body(email.toLowerCase(java.util.Locale.ROOT), "password123!", "RIDER")))
                .andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM riders WHERE account_id = (SELECT id FROM accounts WHERE email = ?)", Long.class, email.toLowerCase(java.util.Locale.ROOT))).isZero();
    }

    @Test
    void rejectsAdminWithoutCreatingAccount() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/auth/signup").contentType("application/json").content(body(email, "password123!", "ADMIN")))
                .andExpect(status().isBadRequest());
        assertThat(accounts.existsByEmail(email)).isFalse();
    }

    @Test
    void rejectsInvalidEmailAndShortPassword() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType("application/json").content(body("bad-email", "short", "CUSTOMER")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsPasswordOverBcryptByteLimit() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/auth/signup").contentType("application/json").content(body(email, "가".repeat(25), "CUSTOMER")))
                .andExpect(status().isBadRequest());
        assertThat(accounts.existsByEmail(email)).isFalse();
    }

    @Test
    void rejectsUnknownRoleAndMalformedJson() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType("application/json").content(body("test@example.com", "password123!", "SUPERUSER")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/signup").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void doesNotExposeOtherEndpoints() throws Exception {
        mvc.perform(get("/api/accounts/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void concurrentSignupCreatesOnlyOneAccountAndProfile() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var start = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Callable<Integer> signup = () -> {
                start.await();
                return mvc.perform(post("/api/auth/signup").contentType("application/json")
                        .content(body(email, "password123!", "CUSTOMER"))).andReturn().getResponse().getStatus();
            };
            var first = executor.submit(signup);
            var second = executor.submit(signup);
            start.countDown();
            assertThat(java.util.List.of(first.get(30, java.util.concurrent.TimeUnit.SECONDS),
                    second.get(30, java.util.concurrent.TimeUnit.SECONDS))).containsExactlyInAnyOrder(201, 409);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM accounts WHERE email = ?", Long.class, email)).isEqualTo(1L);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM customers WHERE account_id = (SELECT id FROM accounts WHERE email = ?)", Long.class, email)).isEqualTo(1L);
        } finally {
            jdbc.update("DELETE FROM customers WHERE account_id IN (SELECT id FROM accounts WHERE email = ?)", email);
            jdbc.update("DELETE FROM accounts WHERE email = ?", email);
        }
    }
}
