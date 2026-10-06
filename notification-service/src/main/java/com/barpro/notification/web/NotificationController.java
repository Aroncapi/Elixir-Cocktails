package com.barpro.notification.web;

import com.barpro.notification.dto.NotificationRequest;
import com.barpro.notification.dto.NotificationResponse;
import com.barpro.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> crear(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<NotificationResponse> listar(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String type) {
        return notificationService.listar(status, type);
    }

    @PutMapping("/{id}/reenviar")
    @PreAuthorize("hasRole('ADMIN')")
    public NotificationResponse reenviar(@PathVariable Long id) {
        return notificationService.reenviar(id);
    }
}
