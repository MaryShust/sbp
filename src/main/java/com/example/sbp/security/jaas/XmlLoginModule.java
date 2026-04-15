package com.example.sbp.security.jaas;

import com.example.sbp.security.CustomUserDetails;
import com.example.sbp.security.XmlUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.*;

@Slf4j
public class XmlLoginModule implements LoginModule {
    private Subject subject;
    private CallbackHandler callbackHandler;
    private String username;
    private CustomUserDetails userDetails;
    private boolean succeeded = false;
    private boolean commitSucceeded = false;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) throw new LoginException("No CallbackHandler available");

        NameCallback nameCallback = new NameCallback("Username: ");
        PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);

        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        } catch (Exception e) {
            throw new LoginException("Error getting credentials: " + e.getMessage());
        }

        username = nameCallback.getName();
        String password = new String(passwordCallback.getPassword());

        try {
            XmlUserDetailsService userDetailsService = SpringApplicationContextHolder.getBean(XmlUserDetailsService.class);
            PasswordEncoder passwordEncoder = SpringApplicationContextHolder.getBean(PasswordEncoder.class);

            if (!userDetailsService.userExists(username)) {
                throw new LoginException("User not found. Please register first.");
            }

            UserDetails details = userDetailsService.loadUserByUsername(username);
            if (details instanceof CustomUserDetails) {
                userDetails = (CustomUserDetails) details;
            } else {
                throw new LoginException("Invalid user details type");
            }

            if (!verifyPassword(password, userDetails.getPassword(), passwordEncoder)) {
                throw new LoginException("Invalid password");
            }

            if (!userDetails.isEnabled()) {
                throw new LoginException("Account is disabled");
            }

            succeeded = true;
            log.info("JAAS login successful for user: {}", username);
            return true;
        } catch (LoginException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            succeeded = false;
            throw new LoginException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (!succeeded || userDetails == null) return false;

        // Store CustomUserDetails directly in subject's public credentials
        subject.getPublicCredentials().add(userDetails);

        commitSucceeded = true;
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        if (!succeeded) return false;
        if (!commitSucceeded) {
            succeeded = false;
            username = null;
            userDetails = null;
        } else {
            logout();
        }
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().clear();
        subject.getPublicCredentials().clear();
        succeeded = false;
        commitSucceeded = false;
        username = null;
        userDetails = null;
        return true;
    }

    private boolean verifyPassword(String rawPassword, String encodedPassword, PasswordEncoder encoder) {
        String clean = encodedPassword.startsWith("{bcrypt}") ? encodedPassword.substring(8) : encodedPassword;
        return encoder.matches(rawPassword, clean);
    }
}
