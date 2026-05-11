package com.example.sbp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret:defaultSecretKeyForDevelopmentPurposesOnly123456789}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private final XmlUserDetailsService userDetailsService;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        if (userDetails == null) {
            throw new BadCredentialsException("ошибка генерации токена");
        }

        String role = userDetails.getRole().name();

        String privileges = userDetails.getPrivileges().stream()
                .map(Privilege::name)
                .collect(Collectors.joining(","));

        var builder = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", role)
                .claim("privileges", privileges)
                .claim("tokenVersion", userDetails.getTokenVersion())
                .issuedAt(now)
                .expiration(expiryDate);

        if (userDetails.getAccountId() != null) {
            builder.claim("accountId", userDetails.getAccountId());
        }

        if (userDetails.getPhoneNumber() != null) {
            builder.claim("phoneNumber", userDetails.getPhoneNumber());
        }

        return builder.signWith(getSigningKey()).compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Role getRoleFromToken(String token) {
        String role = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
        return Role.fromString(role);
    }

    public List<String> getPrivilegesFromToken(String token) {
        String privStr = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("privileges", String.class);
        return privStr == null || privStr.isEmpty() ? List.of() : List.of(privStr.split(","));
    }

    public Long getAccountIdFromToken(String token) {
        Object accountId = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("accountId");
        return accountId != null ? Long.parseLong(accountId.toString()) : null;
    }

    public String getPhoneNumberFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("phoneNumber", String.class);
    }

    public int getTokenVersionFromToken(String token) {
        Object version = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("tokenVersion");
        return version != null ? Integer.parseInt(version.toString()) : 0;
    }

    public boolean validateToken(String token) throws ExpiredJwtException {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);

            // Check token version
            String username = getUsernameFromToken(token);
            int tokenVersion = getTokenVersionFromToken(token);
            int currentVersion = userDetailsService.getTokenVersion(username);

            if (tokenVersion < currentVersion) {
                log.warn("Token version mismatch for user {}: token={}, current={}",
                        username, tokenVersion, currentVersion);
                return false;
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
