package com.barpro.core.web.admin;

import com.barpro.core.dto.RequestEventoResponse;
import com.barpro.core.dto.RequestResumenResponse;
import com.barpro.core.dto.SolicitudPageResponse;
import com.barpro.core.dto.SolicitudResponse;
import com.barpro.core.dto.StatusUpdateRequest;
import com.barpro.core.service.SolicitudPdfService;
import com.barpro.core.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@PreAuthorize("hasRole('ADMIN')")
public class RequestAdminController {

    private final SolicitudService solicitudService;
    private final SolicitudPdfService pdfService;

    public RequestAdminController(SolicitudService solicitudService,
                                  SolicitudPdfService pdfService) {
        this.solicitudService = solicitudService;
        this.pdfService = pdfService;
    }

    @GetMapping
    public SolicitudPageResponse list(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) String search,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return solicitudService.pageAdmin(status, search, page, size);
    }

    @GetMapping("/resumen")
    public RequestResumenResponse resumen() {
        return solicitudService.resumen();
    }

    @GetMapping("/eventos")
    public List<RequestEventoResponse> eventos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return solicitudService.eventos(desde, hasta);
    }

    @GetMapping("/{id}")
    public SolicitudResponse get(@PathVariable Long id) {
        return solicitudService.getAdmin(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        SolicitudResponse solicitud = solicitudService.getAdmin(id);
        byte[] bytes = pdfService.generar(solicitud);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(solicitud.folio() + ".pdf")
                .build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    public SolicitudResponse updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody StatusUpdateRequest request) {
        return solicitudService.updateStatus(id, request.status());
    }
}
