package com.barpro.core.web.publicapi;

import com.barpro.core.dto.SettingsResponse;
import com.barpro.core.service.SettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicSettingsController {

    private final SettingsService settingsService;

    public PublicSettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/settings")
    public SettingsResponse get() {
        return settingsService.get();
    }
}
