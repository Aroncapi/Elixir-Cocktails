let tipoCambio = 3.4;

/** Soles por dolar (PEN/USD) usado en las equivalencias. */
export function setTipoCambio(valor: number | null | undefined): void {
  if (Number.isFinite(valor) && (valor ?? 0) > 0) {
    tipoCambio = valor as number;
  }
}

export function obtenerTipoCambio(): number {
  return tipoCambio;
}

const decimales = new Intl.NumberFormat('es-PE', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

/** Formato en soles, moneda base: "S/ 1,004". */
export function soles(valor: number): string {
  return `S/ ${decimales.format(valor)}`;
}

/** Equivalente en dolares de un monto en soles: "US$ 295.29". */
export function dolares(valorEnSoles: number): string {
  return `US$ ${decimales.format(valorEnSoles / tipoCambio)}`;
}

/** Monto doble para totales: "S/ 1,004 (≈ US$ 295.29)". */
export function montoDoble(valorEnSoles: number): string {
  return `${soles(valorEnSoles)} (≈ ${dolares(valorEnSoles)})`;
}
