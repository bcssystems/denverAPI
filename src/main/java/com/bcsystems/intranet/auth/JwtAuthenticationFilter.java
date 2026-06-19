package com.bcsystems.intranet.auth;

import com.bcsystems.intranet.domain.Persona;
import com.bcsystems.intranet.domain.Token;
import com.bcsystems.intranet.repository.PersonaRepository;
import com.bcsystems.intranet.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final PersonaRepository personaRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   TokenRepository tokenRepository,
                                   PersonaRepository personaRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().contains("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Persona persona = personaRepository.findByUsuarioIgnoreCase(username).orElse(null);
            if (persona != null && persona.getActiva()) {
                var storedToken = tokenRepository.findByToken(jwt).orElse(null);
                if (storedToken != null && !storedToken.getIsRevoked() && !storedToken.getIsExpired()) {
                    if (jwtService.isTokenValid(jwt, persona)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
