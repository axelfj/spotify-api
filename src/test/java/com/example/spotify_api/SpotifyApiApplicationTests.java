package com.example.spotify_api;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@SpringBootTest
@AutoConfigureMockMvc
class SpotifyApiApplicationTests {
    @DynamicPropertySource static void testConfiguration(DynamicPropertyRegistry properties) {
        properties.add("SPOTIFY_API_USERS_JSON", () -> "{\"test-user\":\"{bcrypt}" + new BCryptPasswordEncoder(10).encode("test-only-password") + "\"}");
        properties.add("SPOTIFY_CLIENT_ID", () -> "test-client");
        properties.add("SPOTIFY_CLIENT_SECRET", () -> "test-only-secret");
    }
    @Autowired MockMvc mvc;
    @Test void contextLoads() { }
    @Test void rejectsAnonymousAndFormerSharedCredentials() throws Exception {
        mvc.perform(get("/track/")).andExpect(status().isUnauthorized());
        mvc.perform(get("/track/").with(httpBasic("admin", "password"))).andExpect(status().isUnauthorized());
    }
    @Test void acceptsAConfiguredUser() throws Exception {
        mvc.perform(get("/track/").with(httpBasic("test-user", "test-only-password"))).andExpect(status().isOk());
    }
}
