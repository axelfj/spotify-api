package com.example.spotify_api.Config;

import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.ArrayList;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(@Value("${SPOTIFY_API_USERS_JSON}") String configuredUsers) throws Exception {
        Map<String, String> accounts = new ObjectMapper().readValue(configuredUsers, new TypeReference<Map<String,String>>() {});
        if (accounts.isEmpty()) throw new IllegalStateException("At least one API user is required");
        ArrayList<UserDetails> users = new ArrayList<>();
        for (Map.Entry<String, String> account : accounts.entrySet()) {
            if (account.getKey().isBlank() || account.getKey().contains(":")) throw new IllegalStateException("Invalid API username");
            String hash = account.getValue();
            if (hash == null || !hash.matches("\\{bcrypt\\}\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}")) {
                throw new IllegalStateException("API users must have bcrypt hashes with the {bcrypt} prefix");
            }
            users.add(User.withUsername(account.getKey()).password(hash).roles("USER").build());
        }
        return new InMemoryUserDetailsManager(users);
    }

    @Configuration
    public static class WebConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        }
    }
}
