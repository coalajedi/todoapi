package com.coaladev.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.coaladev.todolist.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        var servletPath = request.getServletPath();

        if (servletPath.startsWith("/tasks/")) {
            var authorization = request.getHeader("Authorization").substring("Basic".length()).trim();

            byte[] decodedAuth = Base64.getDecoder().decode(authorization);

            String authString = new String(decodedAuth);

            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            var user = this.userRepository.findByUsername(username);

            if (user == null) {
                response.sendError(401);
            } else {
                var passwordIsValid = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword()).verified;

                if (passwordIsValid) {
                    request.setAttribute("userId", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
