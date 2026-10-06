package com.barpro.core.web.admin;

import com.barpro.core.dto.SettingsResponse;
import com.barpro.core.dto.SettingsUpdateRequest;
import com.barpro.core.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@PreAuthorize("hasRole('ADMIN')")
public class SettingAdminController {

    private final SettingsService settingsService;

    public SettingAdminController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public SettingsResponse get() {
        return settingsService.get();
    }

    @PutMapping
    public SettingsResponse update(@Valid @RequestBody SettingsUpdateRequest request) {
        return settingsService.update(request);
    }
}
