package com.smartdesk.api.identity.web;

import com.smartdesk.api.identity.model.RoleCode;
import jakarta.validation.constraints.NotNull;

public record AdminUserUpdateRequest(
        @NotNull(message = "Role is required.") RoleCode role,
        boolean enabled,
        boolean accountLocked
) {
}
