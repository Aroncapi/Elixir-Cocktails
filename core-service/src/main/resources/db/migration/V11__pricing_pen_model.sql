-- Modelo de precios en soles (PEN), moneda base de la plataforma.
-- Equipo asignado automaticamente por numero de invitados; sin nivel PREMIUM
-- y sin cobro por invitado. El tipo de cambio a USD es configurable.

UPDATE service_tiers SET price = 272.00 WHERE code = 'BASE';      -- 1 bartender, minimo 5 horas
UPDATE service_tiers SET price = 0.00   WHERE code = 'PREMIUM';   -- el nivel deja de existir
UPDATE service_tiers SET price = 150.00 WHERE code = 'PERSONAL';  -- ayudante, minimo 5 horas

INSERT INTO app_settings (setting_key, setting_value) VALUES
('exchange_rate', '3.4'),       -- soles por dolar (PEN/USD)
('base_hours', '5'),            -- horas minimas cobradas del paquete base
('extra_hour_pen', '40');       -- soles por hora extra, por bartender y por ayudante

ALTER TABLE requests
    ADD COLUMN bartenders INT NOT NULL DEFAULT 1,
    ADD COLUMN assistants INT NOT NULL DEFAULT 0;
