package com.barpro.core.web.publicapi;

import com.barpro.core.dto.SolicitudRequest;
import com.barpro.core.dto.SolicitudResponse;
import com.barpro.core.service.SolicitudPdfService;
import com.barpro.core.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/solicitudes")
public class PublicSolicitudController {

    private final SolicitudService solicitudService;
    private final SolicitudPdfService pdfService;

    public PublicSolicitudController(SolicitudService solicitudService,
                                     SolicitudPdfService pdfService) {
        this.solicitudService = solicitudService;
        this.pdfService = pdfService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudResponse create(@Valid @RequestBody SolicitudRequest request) {
        return solicitudService.create(request);
    }

    @GetMapping("/{token}")
    public SolicitudResponse byToken(@PathVariable String token) {
        return solicitudService.getByToken(token);
    }

    @GetMapping("/{token}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String token) {
        SolicitudResponse solicitud = solicitudService.getByToken(token);
        return pdf(solicitud);
    }

    private ResponseEntity<byte[]> pdf(SolicitudResponse solicitud) {
        byte[] bytes = pdfService.generar(solicitud);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(solicitud.folio() + ".pdf")
                .build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
