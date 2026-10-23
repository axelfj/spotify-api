package com.example.spotify_api;
import com.example.spotify_api.Config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.junit.jupiter.api.Assertions.*;
class SecurityAccountsTest {
    @Test void acceptsDistinctHashedUsersAndRejectsTheOldDemoLogin() throws Exception {
        String hash = new BCryptPasswordEncoder(10).encode("unit-test-only-password");
        var users = new SecurityConfig().userDetailsService("{\"alice\":\"{bcrypt}" + hash + "\",\"bob\":\"{bcrypt}" + hash + "\"}");
        assertEquals("alice", users.loadUserByUsername("alice").getUsername());
        assertTrue(new BCryptPasswordEncoder().matches("unit-test-only-password", users.loadUserByUsername("bob").getPassword().substring(8)));
        assertThrows(UsernameNotFoundException.class, () -> users.loadUserByUsername("admin"));
    }
    @Test void refusesMissingUsersAndPlaintextPasswords() {
        assertThrows(IllegalStateException.class, () -> new SecurityConfig().userDetailsService("{}"));
        assertThrows(IllegalStateException.class, () -> new SecurityConfig().userDetailsService("{\"alice\":\"{noop}password\"}"));
        assertThrows(IllegalStateException.class, () -> new SecurityConfig().userDetailsService("{\"alice\":\"password\"}"));
    }
}
