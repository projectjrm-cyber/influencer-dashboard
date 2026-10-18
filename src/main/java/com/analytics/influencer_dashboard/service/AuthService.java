package com.analytics.influencer_dashboard.service;

import com.analytics.influencer_dashboard.entity.AppUser;
import com.analytics.influencer_dashboard.entity.Role;
import com.analytics.influencer_dashboard.repository.AppUserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Empty = registration disabled (fails closed)
    @Value("${app.registration.code:}")
    private String registrationCode;

    private String dummyHash;

    @PostConstruct
    void init() {
        // Used so unknown usernames take as long to check as real ones
        dummyHash = passwordEncoder.encode("placeholder-password");
    }

    @Transactional
    public AppUser register(String fullName, String username, String password,
                            String confirmPassword, String roleText, String code) {

        if (registrationCode == null || registrationCode.isBlank()) {
            throw new AuthException("Registration is currently disabled.");
        }
        // Code is checked first so people without it learn nothing else
        if (code == null || !MessageDigest.isEqual(
                code.trim().getBytes(StandardCharsets.UTF_8),
                registrationCode.trim().getBytes(StandardCharsets.UTF_8))) {
            throw new AuthException("Invalid registration code.");
        }

        fullName = fullName == null ? "" : fullName.trim();
        username = normalize(username);
        Role role = Role.fromText(roleText);

        if (fullName.isBlank() || fullName.length() > 100) {
            throw new AuthException("Please enter your name.");
        }
        if (!username.matches("[a-z0-9@._-]{3,100}")) {
            throw new AuthException("Username can use letters, numbers and @ . _ - (3 to 100 characters).");
        }
        if (password == null || password.length() < 8) {
            throw new AuthException("Password must be at least 8 characters.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new AuthException("Password is too long (maximum 72 characters).");
        }
        if (!password.equals(confirmPassword)) {
            throw new AuthException("Passwords do not match.");
        }
        if (role == null) {
            throw new AuthException("Please choose a dashboard.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new AuthException("That username is already taken.");
        }

        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new AuthException("That username is already taken.");
        }
    }

    @Transactional(readOnly = true)
    public AppUser authenticate(String username, String password) {
        AppUser user = userRepository.findByUsername(normalize(username)).orElse(null);
        String hash = user != null ? user.getPasswordHash() : dummyHash;
        boolean matches = passwordEncoder.matches(password == null ? "" : password, hash);

        if (user == null || !matches) {
            throw new AuthException("Invalid username or password.");
        }
        return user;
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}