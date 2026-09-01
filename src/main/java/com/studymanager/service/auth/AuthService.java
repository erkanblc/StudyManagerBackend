package com.studymanager.service.auth;

import com.studymanager.config.security.JwtUtils;
import com.studymanager.dto.request.LoginRequest;
import com.studymanager.dto.response.AuthResponse;
import com.studymanager.entity.user.AdminApprovalStatus;
import com.studymanager.entity.user.LoginHistory;
import com.studymanager.entity.user.RefreshToken;
import com.studymanager.entity.user.User;
import com.studymanager.repository.user.LoginHistoryRepository;
import com.studymanager.repository.user.RefreshTokenRepository;
import com.studymanager.repository.user.UserRepository;
import com.studymanager.service.goal.GoalOverdueService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final GoalOverdueService goalOverdueService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       GoalOverdueService goalOverdueService,
                       LoginHistoryRepository loginHistoryRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       @Value("${jwt.refresh-expiration}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.goalOverdueService = goalOverdueService;
        this.loginHistoryRepository = loginHistoryRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        if (user.getAdminApprovalStatus() == AdminApprovalStatus.PENDING) {
            throw new RuntimeException("Your admin account is pending approval by an administrator.");
        }
        if (user.getAdminApprovalStatus() == AdminApprovalStatus.REJECTED) {
            throw new RuntimeException("Your admin application was rejected.");
        }
        if (!user.isActive()) {
            throw new RuntimeException("Account is not active.");
        }

        if (user.getAdminApprovalStatus() == null
                && user.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName()))
                && user.isActive()) {
            user.setAdminApprovalStatus(AdminApprovalStatus.APPROVED);
            userRepository.save(user);
        } else if (user.getAdminApprovalStatus() == null) {
            user.setAdminApprovalStatus(AdminApprovalStatus.NONE);
            userRepository.save(user);
        }

        goalOverdueService.syncOverdueGoalsForUser(user.getId());

        ZonedDateTime previousLoginAt = loginHistoryRepository
                .findTopByUserIdOrderByLoginAtDesc(user.getId())
                .map(LoginHistory::getLoginAt)
                .orElse(null);

        loginHistoryRepository.save(new LoginHistory(user, ZonedDateTime.now(ZoneOffset.UTC)));

        // One active refresh chain per login; revoke older refresh tokens for this user
        refreshTokenRepository.revokeAllForUser(user.getId());

        ZonedDateTime absoluteExpiresAt = ZonedDateTime.now(ZoneOffset.UTC)
                .plusSeconds(refreshExpirationMs / 1000);
        return issueTokens(user, previousLoginAt, absoluteExpiresAt);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new RuntimeException("Refresh token is required.");
        }

        RefreshToken stored = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue.trim())
                .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token."));

        if (stored.isExpired()) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new RuntimeException("Invalid or expired refresh token.");
        }

        User user = stored.getUser();
        if (user == null || !user.isActive()) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new RuntimeException("Account is not active.");
        }

        // Absolute expiry from original login — rotation must not extend it
        ZonedDateTime absoluteExpiresAt = stored.getExpiresAt();

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(user, null, absoluteExpiresAt);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            return;
        }
        refreshTokenRepository.revokeByToken(refreshTokenValue.trim());
    }

    /**
     * Issues access + refresh tokens.
     * {@code absoluteExpiresAt} is fixed at login (now + 7d) and reused on refresh
     * so the session cannot slide beyond that deadline.
     */
    private AuthResponse issueTokens(User user, ZonedDateTime lastLoginAt,
                                     ZonedDateTime absoluteExpiresAt) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        if (absoluteExpiresAt == null || !absoluteExpiresAt.isAfter(now)) {
            throw new RuntimeException("Invalid or expired refresh token.");
        }

        long remainingMs = Duration.between(now, absoluteExpiresAt).toMillis();
        long accessTtlMs = Math.min(jwtUtils.getAccessExpirationMs(), remainingMs);

        String accessToken = jwtUtils.generateToken(user.getEmail(), accessTtlMs);
        String refreshValue = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        refreshTokenRepository.save(new RefreshToken(refreshValue, user, absoluteExpiresAt));

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        return new AuthResponse(
                user.getId(),
                accessToken,
                refreshValue,
                accessTtlMs,
                user.getEmail(),
                user.getUsername(),
                roles,
                lastLoginAt
        );
    }
}
