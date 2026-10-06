package com.barpro.core.service;

import com.barpro.core.dto.SettingsResponse;
import com.barpro.core.dto.SettingsUpdateRequest;
import com.barpro.core.entity.AppSetting;
import com.barpro.core.repository.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class SettingsService {

    public static final String WHATSAPP_OWNER = "whatsapp_owner";
    public static final String CONTACT_EMAIL = "contact_email";
    public static final String BUSINESS_NAME = "business_name";

    private static final Map<String, String> DEFAULTS = Map.of(
            WHATSAPP_OWNER, "+51937336603",
            CONTACT_EMAIL, "concierge@velvetgilt.com",
            BUSINESS_NAME, "Velvet & Gilt");

    private final AppSettingRepository settings;

    public SettingsService(AppSettingRepository settings) {
        this.settings = settings;
    }

    public SettingsResponse get() {
        return new SettingsResponse(value(WHATSAPP_OWNER), value(CONTACT_EMAIL), value(BUSINESS_NAME));
    }

    @Transactional
    public SettingsResponse update(SettingsUpdateRequest request) {
        save(WHATSAPP_OWNER, normalize(request.whatsappOwner()));
        if (request.contactEmail() != null && !request.contactEmail().isBlank()) {
            save(CONTACT_EMAIL, request.contactEmail().trim());
        }
        if (request.businessName() != null && !request.businessName().isBlank()) {
            save(BUSINESS_NAME, request.businessName().trim());
        }
        return get();
    }

    private String value(String key) {
        return settings.findById(key)
                .map(AppSetting::getSettingValue)
                .orElseGet(() -> DEFAULTS.getOrDefault(key, ""));
    }

    private void save(String key, String valor) {
        AppSetting setting = settings.findById(key).orElseGet(() -> {
            AppSetting nuevo = new AppSetting();
            nuevo.setSettingKey(key);
            return nuevo;
        });
        setting.setSettingValue(valor);
        setting.setUpdatedAt(Instant.now());
        settings.save(setting);
    }

    private String normalize(String whatsapp) {
        String limpio = whatsapp.trim();
        return limpio.startsWith("+") ? limpio : "+" + limpio;
    }
}
