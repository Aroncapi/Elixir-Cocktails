-- Tipo de destilado base del coctel (lista fija), opcional.
ALTER TABLE cocktails ADD COLUMN spirit VARCHAR(30) NULL;

UPDATE cocktails SET spirit = 'TEQUILA'      WHERE name = 'Margarita de Bergamota';
UPDATE cocktails SET spirit = 'WHISKY'       WHERE name = 'Old Fashioned Ahumado';
UPDATE cocktails SET spirit = 'TEQUILA'      WHERE name = 'Paloma Rosa';
UPDATE cocktails SET spirit = 'SIN_ALCOHOL'  WHERE name = 'Mojito Sin Alcohol';
UPDATE cocktails SET spirit = 'GINEBRA'      WHERE name = 'Negroni de Casa';
UPDATE cocktails SET spirit = 'VODKA'        WHERE name = 'Martini de Coco';
UPDATE cocktails SET spirit = 'SIN_ALCOHOL'  WHERE name = 'Limonada Lavanda';
UPDATE cocktails SET spirit = 'RON'          WHERE name = 'Daiquiri de Fresa';
