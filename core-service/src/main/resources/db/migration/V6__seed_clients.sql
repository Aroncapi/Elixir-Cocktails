INSERT INTO clients (name, email, whatsapp, location, tier, total_spent, reservations, last_event, active, created_at)
VALUES ('Elena Rostova', 'elena@example.com', '+52 55 1122 3344', 'Ciudad de Mexico', 'VIP',
        45200.00, 12, 'Gala de Verano (Ago 2023)', TRUE, NOW()),
       ('Arch & Co. Architects', 'events@archco.com', '+52 55 9988 7766', 'Guadalajara', 'CORPORATIVO',
        18500.00, 4, 'Lanzamiento Proyecto X (Nov 2023)', TRUE, NOW()),
       ('David Sterling', 'david.s@example.com', '+52 55 3344 5566', 'Monterrey', 'PARTICULAR',
        5400.00, 1, 'Aniversario Boda (Ene 2023)', FALSE, NOW());
