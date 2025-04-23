package com.example.hospital.config;

import com.example.hospital.model.Role;
import com.example.hospital.repository.UserRepository;
import com.example.hospital.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtRequestFilter jwtRequestFilter;

    @Autowired
    public SecurityConfig(UserRepository userRepository, JwtRequestFilter jwtRequestFilter) {
        this.userRepository = userRepository;
        this.jwtRequestFilter = jwtRequestFilter;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .antMatchers("/auth/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/drugs", "/api/drugs/search", "/api/drugs/categories").permitAll()
                .antMatchers("/", "/js/**", "/css/**", "/assets/**", "/h2-console/**", "/favicon.ico").permitAll()
                .antMatchers("/api/users/**").hasRole(Role.ADMIN.name())
                .antMatchers(HttpMethod.POST, "/api/drugs").hasRole(Role.ADMIN.name())
                .antMatchers(HttpMethod.PUT, "/api/drugs/**").hasRole(Role.ADMIN.name())
                .antMatchers(HttpMethod.DELETE, "/api/drugs/**").hasRole(Role.ADMIN.name())
                .antMatchers(HttpMethod.POST, "/api/drugs/*/batches").hasAnyRole(Role.ADMIN.name(), Role.PHARMACIST.name())
                .antMatchers(HttpMethod.GET, "/api/drugs/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/prescriptions").hasAnyRole(Role.ADMIN.name(), Role.PHARMACIST.name())
                .antMatchers(HttpMethod.PUT, "/api/prescriptions/*/status").hasAnyRole(Role.ADMIN.name(), Role.PHARMACIST.name(), Role.NURSE.name())
                .antMatchers("/api/prescriptions/**").authenticated()
                .antMatchers("/api/reports/**").hasRole(Role.ADMIN.name())
                .antMatchers(HttpMethod.GET, "/api/logs/my").authenticated()
                .antMatchers(HttpMethod.GET, "/api/logs", "/api/logs/drug/**").hasRole(Role.ADMIN.name())
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .headers(headers -> headers.frameOptions().disable());
        
        return http.build();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081", "http://localhost:63342", "http://127.0.0.1:5500", "http://localhost:5173", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            com.example.hospital.model.User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("用户不存在: " + username);
            }
            if (user.getRole() == null) {
                 throw new IllegalStateException("用户 " + username + " 的角色未设置!");
            }
            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
        };
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 