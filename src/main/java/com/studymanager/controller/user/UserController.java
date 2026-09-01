package com.studymanager.controller.user;

import com.studymanager.dto.request.CreateUserRequest;
import com.studymanager.dto.request.UpdateUserRolesRequest;
import com.studymanager.entity.user.User;
import com.studymanager.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "Kullanici yönetimi")
@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Tüm kullanıcıları listele")
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Yeni kullanıcı oluştur")
    @PostMapping
    public User createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(
                request.getUsername(),
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getRoleNames(),
                request.isActive()
        );
    }

    @Operation(summary = "ID ile kullanıcı getir")
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @Operation(summary = "Kullanıcının rollerini güncelle")
    @PutMapping("/{userId}/roles")
    public User updateUserRoles(@PathVariable Long userId,
                                @RequestBody UpdateUserRolesRequest request) {
        return userService.updateUserRoles(userId, request.getRoleNames());
    }

    @Operation(summary = "Kullanıcı aktif/pasif durumunu değiştir")
    @PutMapping("/{userId}/status")
    public User updateUserStatus(@PathVariable Long userId,
                                 @RequestParam boolean active) {
        return userService.updateUserStatus(userId, active);
    }

    @Operation(summary = "Kullanıcı sil")
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
