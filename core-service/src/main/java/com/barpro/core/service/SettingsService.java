package com.barpro.core.service;

import com.barpro.core.dto.SettingsResponse;
import com.barpro.core.dto.SettingsUpdateRequest;
import com.barpro.core.entity.AppSetting;
import com.barpro.core.repository.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Service
public class SettingsService {

    public static final String WHATSAPP_OWNER = "whatsapp_owner";
    public static final String CONTACT_EMAIL = "contact_email";
    public static final String BUSINESS_NAME = "business_name";
    public static final String EXCHANGE_RATE = "exchange_rate";
    public static final String BASE_HOURS = "base_hours";
    public static final String EXTRA_HOUR_PEN = "extra_hour_pen";

    private static final Map<String, String> DEFAULTS = Map.of(
            WHATSAPP_OWNER, "+51937336603",
            CONTACT_EMAIL, "concierge@velvetgilt.com",
            BUSINESS_NAME, "Velvet & Gilt",
            EXCHANGE_RATE, "3.4",
            BASE_HOURS, "5",
            EXTRA_HOUR_PEN, "40");

    private final AppSettingRepository settings;

    public SettingsService(AppSettingRepository settings) {
        this.settings = settings;
    }

    public SettingsResponse get() {
        return new SettingsResponse(
                value(WHATSAPP_OWNER),
                value(CONTACT_EMAIL),
                value(BUSINESS_NAME),
                exchangeRate(),
                baseHours(),
                extraHourPen());
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
        if (request.exchangeRate() != null) {
            save(EXCHANGE_RATE, request.exchangeRate().toPlainString());
        }
        return get();
    }

    /** Soles por dolar (PEN/USD) usado para mostrar la equivalencia en USD. */
    public BigDecimal exchangeRate() {
        return decimal(EXCHANGE_RATE, new BigDecimal("3.4"));
    }

    /** Horas minimas cobradas del paquete base. */
    public int baseHours() {
        try {
            return Math.max(1, Integer.parseInt(value(BASE_HOURS)));
        } catch (NumberFormatException ex) {
            return 5;
        }
    }

    /** Soles por hora extra, por bartender y por ayudante. */
    public BigDecimal extraHourPen() {
        return decimal(EXTRA_HOUR_PEN, new BigDecimal("40"));
    }

    private BigDecimal decimal(String key, BigDecimal porDefecto) {
        try {
            return new BigDecimal(value(key));
        } catch (NumberFormatException ex) {
            return porDefecto;
        }
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
