package com.studymanager.controller.admin;

import com.studymanager.dto.response.AdminGoalResponse;
import com.studymanager.service.goal.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Goals", description = "Admin — tüm kullanıcıların hedeflerini görüntüleme")
@RestController
@RequestMapping("/api/admin/goals")
public class AdminGoalController {

    private final GoalService goalService;

    public AdminGoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @Operation(summary = "Tüm kullanıcıların hedeflerini listele")
    @GetMapping
    public ResponseEntity<List<AdminGoalResponse>> getAll(
            @RequestParam(required = false) Long userId) {
        if (userId != null) {
            return ResponseEntity.ok(goalService.getGoalsByUserAdmin(userId));
        }
        return ResponseEntity.ok(goalService.getAllGoalsAdmin());
    }

    @Operation(summary = "Belirli kullanıcının hedeflerini listele")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AdminGoalResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(goalService.getGoalsByUserAdmin(userId));
    }
}
