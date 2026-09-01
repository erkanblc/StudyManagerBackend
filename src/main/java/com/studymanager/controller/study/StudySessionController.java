package com.studymanager.controller.study;

import com.studymanager.dto.request.HeartbeatSessionRequest;
import com.studymanager.dto.request.ManualSessionRequest;
import com.studymanager.dto.request.ResolveSessionRequest;
import com.studymanager.dto.request.StartSessionRequest;
import com.studymanager.dto.request.UpdateSessionRequest;
import com.studymanager.dto.response.StudySessionResponse;
import com.studymanager.entity.user.User;
import com.studymanager.service.study.StudySessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Study Sessions", description = "Çalışma seansı yönetimi — listeleme, başlatma, heartbeat, resolve, silme")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/sessions")
public class StudySessionController {

    private final StudySessionService sessionService;

    public StudySessionController(StudySessionService sessionService) {
        this.sessionService = sessionService;
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Operation(summary = "Tüm seansları listele")
    @GetMapping
    public ResponseEntity<List<StudySessionResponse>> getAll(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(sessionService.getAllSessions(currentUser.getId()));
    }

    @Operation(
        summary = "Aktif seansı getir",
        description = "ACTIVE seans varsa döner; yoksa 204 No Content"
    )
    @GetMapping("/active")
    public ResponseEntity<StudySessionResponse> getActive(
            @AuthenticationPrincipal User currentUser) {
        return sessionService.getActiveSession(currentUser.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Toplam çalışma süresi (saniye)")
    @GetMapping("/total")
    public ResponseEntity<Map<String, Long>> getTotal(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                Map.of("totalSeconds", sessionService.getTotalSeconds(currentUser.getId())));
    }

    @Operation(
        summary = "Son çalışma seansı",
        description = "Kullanıcının en son oluşturduğu seans — aktif veya tamamlanmış. Yoksa 204 No Content."
    )
    @GetMapping("/last")
    public ResponseEntity<StudySessionResponse> getLast(
            @AuthenticationPrincipal User currentUser) {
        return sessionService.getLastSession(currentUser.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Stopwatch başlat",
        description = "durationSeconds=0 ve lastHeartbeatAt=now() ile ACTIVE seans açar. " +
                      "Body opsiyonel: goalId, subject, notes gönderilebilir."
    )
    @PostMapping("/start")
    public ResponseEntity<StudySessionResponse> start(
            @AuthenticationPrincipal User currentUser,
            @RequestBody(required = false) StartSessionRequest request) {
        return ResponseEntity.ok(sessionService.startSession(currentUser.getId(), request));
    }

    @Operation(
        summary = "Manuel çalışma ekle",
        description = "startTime+endTime ya da sadece duration (saniye) gönderin."
    )
    @PostMapping("/manual")
    public ResponseEntity<StudySessionResponse> addManual(
            @AuthenticationPrincipal User currentUser,
            @RequestBody ManualSessionRequest request) {
        return ResponseEntity.ok(
                sessionService.createManualSession(currentUser.getId(), request));
    }

    @Operation(summary = "Stopwatch durdur (legacy)", description = "ACTIVE seansı kapatır")
    @PostMapping("/stop")
    public ResponseEntity<StudySessionResponse> stop(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(sessionService.stopSession(currentUser.getId()));
    }

    // ── HEARTBEAT ─────────────────────────────────────────────────────────────

    @Operation(
        summary = "Heartbeat gönder",
        description = "ACTIVE seansın durationSeconds ve lastHeartbeatAt alanlarını günceller. " +
                      "duration zorunlu. goalId/subject/notes opsiyonel olarak güncellenebilir."
    )
    @PatchMapping("/{sessionId}/heartbeat")
    public ResponseEntity<StudySessionResponse> heartbeat(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal User currentUser,
            @RequestBody HeartbeatSessionRequest request) {
        return ResponseEntity.ok(
                sessionService.heartbeat(sessionId, currentUser.getId(), request));
    }

    // ── RESOLVE ───────────────────────────────────────────────────────────────

    @Operation(
        summary = "Seansı sonlandır / devam et",
        description = """
            action değerleri:
            - SAVE_AT_HEARTBEAT → lastHeartbeatAt'ı endTime olarak kullan, COMPLETED yap
            - CONTINUE          → seans ACTIVE kalmaya devam eder, lastHeartbeatAt güncellenir
            - MANUAL            → startTime+endTime veya duration ile COMPLETED yap
            """
    )
    @PostMapping("/{sessionId}/resolve")
    public ResponseEntity<StudySessionResponse> resolve(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal User currentUser,
            @RequestBody ResolveSessionRequest request) {
        return ResponseEntity.ok(
                sessionService.resolveSession(sessionId, currentUser.getId(), request));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Operation(summary = "Seans güncelle", description = "Null gönderilen alanlar değiştirilmez")
    @PutMapping("/{sessionId}")
    public ResponseEntity<StudySessionResponse> update(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateSessionRequest request) {
        return ResponseEntity.ok(
                sessionService.updateSession(sessionId, currentUser.getId(), request));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Operation(summary = "Seans sil")
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal User currentUser) {
        sessionService.deleteSession(sessionId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
