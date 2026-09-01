package com.studymanager.service.user;

import com.studymanager.dto.request.LoginHistoryRequest;
import com.studymanager.dto.response.LoginHistoryResponse;
import com.studymanager.entity.user.LoginHistory;
import com.studymanager.repository.user.LoginHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    public LoginHistoryService(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    public List<LoginHistoryResponse> getMyHistory(Long userId) {
        return loginHistoryRepository.findByUserIdOrderByLoginAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LoginHistoryResponse getById(Long id, Long userId) {
        LoginHistory entry = findForUser(id, userId);
        return toResponse(entry);
    }

    @Transactional
    public LoginHistoryResponse update(Long id, Long userId, LoginHistoryRequest request) {
        if (request.getLoginAt() == null) {
            throw new IllegalArgumentException("loginAt cannot be null");
        }
        LoginHistory entry = findForUser(id, userId);
        entry.setLoginAt(request.getLoginAt());
        return toResponse(loginHistoryRepository.save(entry));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        LoginHistory entry = findForUser(id, userId);
        loginHistoryRepository.delete(entry);
    }

    @Transactional
    public void deleteAll(Long userId) {
        List<LoginHistory> entries = loginHistoryRepository.findByUserIdOrderByLoginAtDesc(userId);
        loginHistoryRepository.deleteAll(entries);
    }

    private LoginHistory findForUser(Long id, Long userId) {
        LoginHistory entry = loginHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Login history entry not found: " + id));
        if (!entry.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        return entry;
    }

    // ── ADMIN (no ownership check) ────────────────────────────────────────────

    public LoginHistoryResponse getByIdAdmin(Long id) {
        return toResponse(loginHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Login history entry not found: " + id)));
    }

    @Transactional
    public LoginHistoryResponse updateAdmin(Long id, LoginHistoryRequest request) {
        if (request.getLoginAt() == null) {
            throw new IllegalArgumentException("loginAt cannot be null");
        }
        LoginHistory entry = loginHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Login history entry not found: " + id));
        entry.setLoginAt(request.getLoginAt());
        return toResponse(loginHistoryRepository.save(entry));
    }

    @Transactional
    public void deleteAdmin(Long id) {
        LoginHistory entry = loginHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Login history entry not found: " + id));
        loginHistoryRepository.delete(entry);
    }

    private LoginHistoryResponse toResponse(LoginHistory entry) {
        return new LoginHistoryResponse(entry.getId(), entry.getLoginAt());
    }
}
