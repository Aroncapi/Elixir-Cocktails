package com.barpro.core.service;

import com.barpro.core.dto.SolicitudResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Genera el PDF descargable de una solicitud (Fase 5) con openhtmltopdf-pdfbox.
 * OJO: el HTML se parsea como XML, debe ser XHTML bien formado (tags cerrados,
 * & escapado, sin '<' literal en el texto).
 */
@Service
public class SolicitudPdfService {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("es", "MX"));

    public byte[] generar(SolicitudResponse solicitud) {
        String html = plantilla(solicitud);

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(salida);
            builder.run();
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el PDF de la solicitud", ex);
        }
    }

    private String plantilla(SolicitudResponse s) {
        StringBuilder html = new StringBuilder(4096);
        html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ")
                .append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">")
                .append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\"><head>")
                .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>")
                .append("<title>Cotizacion ").append(esc(s.folio())).append("</title>")
                .append("<style>")
                .append("@page { size: A4; margin: 18mm 16mm; }")
                .append("body { font-family: Helvetica, sans-serif; font-size: 10.5pt; color: #1a1a1a; line-height: 1.45; }")
                .append("h1 { font-family: Georgia, serif; font-size: 26pt; margin: 0 0 2pt 0; }")
                .append(".marca { color: #8a6d1a; letter-spacing: 2pt; font-size: 9pt; text-transform: uppercase; }")
                .append(".sub { margin: 0; color: #555; font-size: 9pt; }")
                .append(".rule { border-bottom: 1px solid #b9b9b9; margin: 12pt 0 14pt 0; }")
                .append("h2 { font-family: Georgia, serif; font-size: 12pt; margin: 16pt 0 6pt 0; color: #8a6d1a; }")
                .append("table { width: 100%; border-collapse: collapse; }")
                .append("td { padding: 3pt 0; vertical-align: top; }")
                .append("td.k { width: 42%; color: #666; }")
                .append("td.v { color: #1a1a1a; }")
                .append(".badge { border: 1px solid #8a6d1a; color: #8a6d1a; padding: 1pt 6pt; font-size: 8.5pt; text-transform: uppercase; }")
                .append("ul { margin: 4pt 0 0 14pt; padding: 0; }")
                .append("li { margin-bottom: 4pt; }")
                .append("li strong { font-size: 10.5pt; }")
                .append("li span { color: #555; }")
                .append(".linea { border-bottom: 1px solid #e2e2e2; padding: 5pt 0; }")
                .append(".linea span { color: #555; }")
                .append(".total { margin-top: 10pt; border: 1px solid #b9b9b9; padding: 8pt 10pt; }")
                .append(".total .et { font-family: Georgia, serif; font-size: 13pt; }")
                .append(".total .im { font-family: Georgia, serif; font-size: 15pt; color: #8a6d1a; }")
                .append(".pie { margin-top: 22pt; padding-top: 8pt; border-top: 1px solid #b9b9b9; font-size: 8.5pt; color: #777; }")
                .append("strong { font-weight: bold; }")
                .append("</style></head><body>");

        html.append("<div class=\"marca\">Velvet &amp; Gilt · Servicios de mixologia para eventos</div>")
                .append("<h1>Cotizacion de evento</h1>")
                .append("<p class=\"sub\">Folio <strong>#").append(esc(s.folio())).append("</strong> &nbsp;·&nbsp; ")
                .append("<span class=\"badge\">").append(esc(estado(s.status()))).append("</span> &nbsp;·&nbsp; ")
                .append("Emitido ").append(FECHA.format(Instant.now().atZone(ZoneId.of("America/Mexico_City"))))
                .append("</p>")
                .append("<div class=\"rule\"></div>");

        html.append("<h2>Informacion del cliente</h2>")
                .append(tabla(new String[][]{
                        {"Nombre", s.client().name()},
                        {"Correo", valor(s.client().email())},
                        {"WhatsApp", valor(s.client().whatsapp())},
                        {"Ubicacion del evento", valor(s.location())},
                }));

        html.append("<h2>Detalles del evento</h2>")
                .append(tabla(new String[][]{
                        {"Tipo de evento", s.eventTypeName()},
                        {"Fecha", fecha(s.eventDate()) + (s.eventTime() == null ? "" : " · " + s.eventTime() + " hrs")},
                        {"Invitados", s.guests() + " personas"},
                        {"Duracion", s.durationHours() + " horas"},
                        {"Nivel de servicio", "PREMIUM".equals(s.level())
                                ? "Mixologia Premium" : "Mixologia Base"},
                        {"Bartenders adicionales", String.valueOf(s.extraBartenders())},
                        {"Notas / alergias", valor(s.notes())},
                }));

        html.append("<h2>Menu seleccionado</h2><ul>");
        for (SolicitudResponse.CocktailRef coctel : s.cocktails()) {
            html.append("<li><strong>").append(esc(coctel.name())).append("</strong>")
                    .append("<br/><span>").append(esc(coctel.ingredients())).append("</span></li>");
        }
        if (s.cocktails().isEmpty()) {
            html.append("<li>Sin cocteles seleccionados.</li>");
        }
        html.append("</ul>");

        html.append("<h2>Desglose financiero</h2>")
                .append(linea("Servicio Base (" + s.durationHours() + " horas)", dinero(s.breakdown().base())));
        if (s.breakdown().premium().signum() > 0) {
            html.append(linea("Menu Premium (" + s.guests() + " pax)", dinero(s.breakdown().premium())));
        }
        if (s.breakdown().personal().signum() > 0) {
            html.append(linea("Personal Adicional (" + s.extraBartenders() + " Bartenders)",
                    dinero(s.breakdown().personal())));
        }
        html.append("<div class=\"total\"><table><tr>")
                .append("<td class=\"et\"><strong>Total Estimado</strong></td>")
                .append("<td class=\"im\" style=\"text-align: right;\"><strong>")
                .append(dinero(s.breakdown().total()))
                .append("</strong></td></tr></table></div>");

        html.append("<div class=\"pie\">Documento generado automaticamente por Velvet &amp; Gilt. ")
                .append("Los montos son estimaciones y se confirman con el equipo de planners. ")
                .append("Conserva el folio <strong>#").append(esc(s.folio())).append("</strong> para dar seguimiento.</div>");

        html.append("</body></html>");
        return html.toString();
    }

    private String tabla(String[][] filas) {
        StringBuilder sb = new StringBuilder("<table>");
        for (String[] fila : filas) {
            sb.append("<tr><td class=\"k\">").append(esc(fila[0]))
                    .append("</td><td class=\"v\">").append(esc(fila[1])).append("</td></tr>");
        }
        return sb.append("</table>").toString();
    }

    private String linea(String etiqueta, String importe) {
        return "<div class=\"linea\"><table><tr>"
                + "<td><span>" + esc(etiqueta) + "</span></td>"
                + "<td style=\"text-align: right;\"><strong>" + esc(importe) + "</strong></td>"
                + "</tr></table></div>";
    }

    private String estado(String status) {
        return switch (status) {
            case "CONFIRMADO" -> "Confirmado";
            case "CANCELADO" -> "Cancelado";
            default -> "Pendiente";
        };
    }

    private String fecha(String iso) {
        if (iso == null || iso.isBlank()) {
            return "Por definir";
        }
        String[] p = iso.split("-");
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return Integer.parseInt(p[2]) + " de " + meses[Integer.parseInt(p[1]) - 1] + " de " + p[0];
    }

    private String dinero(BigDecimal valor) {
        java.text.NumberFormat formato = java.text.NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        formato.setMinimumFractionDigits(0);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }

    private String esc(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
