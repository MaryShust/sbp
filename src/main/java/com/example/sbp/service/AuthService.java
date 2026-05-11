package com.example.sbp.service;

import com.example.sbp.dto.UserResponseDTO;
import com.example.sbp.security.CustomUserDetails;
import com.example.sbp.security.JwtTokenProvider;
import com.example.sbp.security.XmlUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final XmlUserDetailsService userDetailsService;

    @Transactional
    public String login(String username, String password) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
        authenticationManager.authenticate(authToken);

        userDetailsService.incrementTokenVersion(username);

        CustomUserDetails userDetails = userDetailsService.getUser(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenProvider.generateToken(authentication);
    }

    @Transactional
    public String register(String username, String password, String phoneNumber) {
        userDetailsService.createUserWithPhone(username, password, phoneNumber);

        CustomUserDetails userDetails = userDetailsService.getUser(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenProvider.generateToken(authentication);
    }

    public UserResponseDTO getUser(String username) {
        CustomUserDetails user = userDetailsService.getUser(username);
        return new UserResponseDTO(user.getUsername(), user.getRole());
    }

    @Transactional
    public UserResponseDTO updateUserRole(String username, String role) {
        userDetailsService.updateUserRole(username, role);
        CustomUserDetails user = userDetailsService.getUser(username);
        return new UserResponseDTO(user.getUsername(), user.getRole());
    }

    @Transactional
    public int logout(String username) {
        return userDetailsService.incrementTokenVersion(username);
    }
}
