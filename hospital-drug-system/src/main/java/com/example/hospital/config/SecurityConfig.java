package com.example.hospital.config;

import com.example.hospital.repository.UserRepository;
import com.example.hospital.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
            .cors().and().csrf().disable()  // 在生产环境中应启用CSRF保护
            .authorizeRequests()
                .antMatchers("/api/users/login", "/api/users/register", "/api/users/check").permitAll()
                .antMatchers("/api/test/**").permitAll()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/api/drugs/admin/**").hasRole("ADMIN")
                .antMatchers("/api/settings/**").hasRole("ADMIN")
                .antMatchers("/api/drugs/**").permitAll()
                .antMatchers("/", "/js/**", "/css/**", "/assets/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 不创建session
                .and()
            .formLogin()
                .disable()
            .logout()
                .permitAll()
                .and()
            .headers().frameOptions().disable(); // 为了H2控制台可以正常访问
        
        // 添加JWT过滤器
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081", "http://localhost:63342", "http://127.0.0.1:5500", "http://localhost:5173"));
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
                throw new UsernameNotFoundException("用户不存在");
            }
            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole()))
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