package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.service.AdminUserService;
import com.smartdesk.api.identity.service.AdminUserSummary;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserResponse> listUsers() {
        return adminUserService.listUsers().stream()
                .map(AdminUserController::toResponse)
                .toList();
    }

    @PatchMapping("/{userId}")
    public AdminUserResponse updateUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        return toResponse(adminUserService.updateUser(
                UUID.fromString(jwt.getSubject()),
                userId,
                request.role(),
                request.enabled(),
                request.accountLocked()
        ));
    }

    private static AdminUserResponse toResponse(AdminUserSummary user) {
        return new AdminUserResponse(
                user.id(),
                user.email(),
                user.fullName(),
                user.roles(),
                user.enabled(),
                user.accountLocked(),
                user.createdAt()
        );
    }
}
