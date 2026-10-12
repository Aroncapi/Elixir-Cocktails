/** Lista fija de destilados (debe coincidir con el enum CocktailSpirit en backend). */
export const DESTILADOS = [
  'TEQUILA',
  'RON',
  'VODKA',
  'GINEBRA',
  'WHISKY',
  'BRANDY',
  'APERITIVO',
  'SIN_ALCOHOL',
  'OTRO',
] as const;

export const ETIQUETAS_DESTILADO: Record<string, string> = {
  TEQUILA: 'Tequila',
  RON: 'Ron',
  VODKA: 'Vodka',
  GINEBRA: 'Ginebra',
  WHISKY: 'Whisky',
  BRANDY: 'Brandy',
  APERITIVO: 'Aperitivo',
  SIN_ALCOHOL: 'Sin alcohol',
  OTRO: 'Otro',
};
