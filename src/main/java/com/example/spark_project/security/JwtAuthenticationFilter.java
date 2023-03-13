package com.example.spark_project.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        //get authorization out of our header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        //check if auth exists, and it is Bearer type
        //if not send request and response to next filter in our filter chain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //if it does exist extract token to our previously created variable jwt
        jwt = authHeader.substring(7);
        //extract username (in our case users email) out of token
        username = jwtService.extractUsername(jwt);
        //then check if username exists and if it already isn't authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //if that is all true fetch our user from db
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            //then check if token is valid
            if (jwtService.isTokenValid(jwt, userDetails)) {
                //if it is create UsernamePasswordAuthenticationToken
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                //add additional details to it
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                //and finally update security context holder
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        //else send request and response to next filter in our filter chain
        filterChain.doFilter(request, response);
    }
}
