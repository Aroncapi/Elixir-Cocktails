INSERT INTO event_types (code, name, description, active, sort_order) VALUES
    ('BODA', 'Bodas', 'Bodas, salones y jardines', TRUE, 1),
    ('CORPORATIVO', 'Corporativos', 'Eventos de empresa, lanzamientos y convenciones', TRUE, 2),
    ('FIESTA_PRIVADA', 'Fiestas Privadas', 'Cumpleanos, aniversarios y reuniones', TRUE, 3),
    ('MASTERCLASS', 'Masterclass', 'Talleres y degustaciones guiadas', TRUE, 4);

INSERT INTO service_tiers (code, name, description, tier_type, price, active) VALUES
    ('BASE', 'Servicio Base', 'Bartender + cocteleria base por horas y numero de invitados', 'BASE', NULL, TRUE),
    ('PREMIUM', 'Menu Premium', 'Carta ampliada de cocteles de autor e ingredientes premium', 'PREMIUM', NULL, TRUE),
    ('PERSONAL', 'Personal Adicional', 'Bartenders extra para eventos con mucha afluencia', 'PERSONAL', NULL, TRUE);

INSERT INTO cocktails (name, category, ingredients, image_url, active) VALUES
    ('Margarita de Bergamota', 'SIGNATURE', 'Tequila blanco, licor de bergamota, limon, agave, sal de vainilla', NULL, TRUE),
    ('Old Fashioned Ahumado', 'CLASICOS', 'Bourbon, azúcar moreno, Angostura, hielo ahumado de mezquite', NULL, TRUE),
    ('Paloma Rosa', 'CITRICOS', 'Tequila, toronja rosa, lima, soda, un toque de romero', NULL, TRUE),
    ('Mojito Sin Alcohol', 'SIN_ALCOHOL', 'Hierbabuena, limon, azucar, soda, rodaja de pepino', NULL, TRUE),
    ('Negroni de Casa', 'CLASICOS', 'Gin, vermut rojo, Campari, piel de naranja', NULL, TRUE),
    ('Martini de Coco', 'SIGNATURE', 'Vodka de coco, licor de curacao blanco, crema de coco, ralladura de limon', NULL, TRUE),
    ('Limonada Lavanda', 'SIN_ALCOHOL', 'Limon, sirope de lavanda, agua mineral, ramita de tomillo', NULL, TRUE),
    ('Daiquiri de Fresa', 'CITRICOS', 'Ron blanco, fresa fresca, limon, azucar', NULL, TRUE);

INSERT INTO cocktail_event_types (cocktail_id, event_type_id)
    SELECT c.id, e.id FROM cocktails c CROSS JOIN event_types e
    WHERE c.name = 'Margarita de Bergamota';

INSERT INTO cocktail_event_types (cocktail_id, event_type_id)
    SELECT c.id, e.id FROM cocktails c CROSS JOIN event_types e
    WHERE c.name = 'Old Fashioned Ahumado' AND e.code IN ('CORPORATIVO', 'FIESTA_PRIVADA');

INSERT INTO cocktail_event_types (cocktail_id, event_type_id)
    SELECT c.id, e.id FROM cocktails c CROSS JOIN event_types e
    WHERE c.name = 'Paloma Rosa' AND e.code IN ('BODA', 'FIESTA_PRIVADA', 'MASTERCLASS');

INSERT INTO cocktail_event_types (cocktail_id, event_type_id)
    SELECT c.id, e.id FROM cocktails c CROSS JOIN event_types e
    WHERE c.name = 'Mojito Sin Alcohol';

INSERT INTO cocktail_event_types (cocktail_id, event_type_id)
    SELECT c.id, e.id FROM cocktails c CROSS JOIN event_types e
    WHERE c.name = 'Negroni de Casa' AND e.code IN ('CORPORATIVO', 'MASTERCLASS');

INSERT INTO cocktail_event_types (cocktail_id, event_type_id)
    SELECT c.id, e.id FROM cocktails c CROSS JOIN event_types e
    WHERE c.name = 'Martini de Coco' AND e.code IN ('BODA', 'CORPORATIVO');

INSERT INTO cocktail_event_types (cocktail_id, event_type_id)
    SELECT c.id, e.id FROM cocktails c CROSS JOIN event_types e
    WHERE c.name = 'Limonada Lavanda';

INSERT INTO cocktail_event_types (cocktail_id, event_type_id)
    SELECT c.id, e.id FROM cocktails c CROSS JOIN event_types e
    WHERE c.name = 'Daiquiri de Fresa' AND e.code IN ('BODA', 'FIESTA_PRIVADA');
