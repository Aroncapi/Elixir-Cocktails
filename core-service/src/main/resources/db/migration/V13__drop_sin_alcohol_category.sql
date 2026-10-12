-- La categoria SIN_ALCOHOL deja de existir: pasa a ser solo un destilado (spirit).
-- Los cocteles afectados quedan en SIGNATURE y conservan spirit = SIN_ALCOHOL.
UPDATE cocktails SET spirit = 'SIN_ALCOHOL' WHERE category = 'SIN_ALCOHOL';
UPDATE cocktails SET category = 'SIGNATURE' WHERE category = 'SIN_ALCOHOL';
