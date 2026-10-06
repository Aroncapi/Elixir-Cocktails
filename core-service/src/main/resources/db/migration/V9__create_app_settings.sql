CREATE TABLE app_settings (
    setting_key VARCHAR(60) NOT NULL PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO app_settings (setting_key, setting_value) VALUES
('whatsapp_owner', '+51937336603'),
('contact_email', 'concierge@velvetgilt.com'),
('business_name', 'Velvet & Gilt');
