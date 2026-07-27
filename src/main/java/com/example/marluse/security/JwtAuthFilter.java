package com.example.marluse.security;

import jakarta.servlet.*;
    import jakarta.servlet.http.*;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.userdetails.*;
    import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
    import org.springframework.stereotype.Component;
    import org.springframework.web.filter.OncePerRequestFilter;
    import com.example.marluse.security.service.JwtService;

    import java.io.IOException;
    import java.util.Arrays;
    @Component
    @RequiredArgsConstructor
    public class JwtAuthFilter extends OncePerRequestFilter {
        private final JwtService jwtService;
        private final UserDetailsService userDetailsService;

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

            String token = null;
            if (request.getCookies() != null ) {
                token = Arrays.stream(request.getCookies())
                        .filter(c -> "access_token".equals(c.getName()))
                        .map(Cookie::getValue)
                        .findFirst()
                        .orElse(null);
            }

            if (token == null) {
                chain.doFilter(request, response);
                return;
            }

            try {
                final String username = jwtService.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(token, userDetails)) {
                        var authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Token inválido ou expirado — continua sem autenticar; Spring retorna 401
            }
            chain.doFilter(request,response);
        }
    }
