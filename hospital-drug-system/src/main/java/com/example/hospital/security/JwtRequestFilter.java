package com.example.hospital.security;

import com.example.hospital.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        
        logger.debug("处理请求: " + requestURI + ", Authorization 头: " + (authorizationHeader != null ? "存在" : "不存在"));

        String username = null;
        String jwt = null;

        // 检查请求头中是否包含JWT，且格式为"Bearer token"
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("从JWT中提取的用户名: " + username);
            } catch (Exception e) {
                logger.error("JWT令牌无效: " + e.getMessage(), e);
            }
        } else if (authorizationHeader != null) {
            logger.warn("Authorization头格式不正确，应以'Bearer '开头: " + authorizationHeader);
        }

        // 如果从JWT中提取到用户名，且当前SecurityContext中没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 验证JWT有效
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 更新SecurityContext中的认证信息
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.debug("认证成功，用户: " + username + ", 权限: " + userDetails.getAuthorities());
            } else {
                logger.warn("JWT令牌验证失败，用户: " + username);
            }
        }
        chain.doFilter(request, response);
    }
} 