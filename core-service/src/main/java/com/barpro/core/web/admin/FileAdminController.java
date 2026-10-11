package com.barpro.core.web.admin;

import com.barpro.core.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@PreAuthorize("hasRole('ADMIN')")
public class FileAdminController {

    private final FileStorageService files;

    public FileAdminController(FileStorageService files) {
        this.files = files;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> upload(@RequestPart("file") MultipartFile file) {
        return Map.of("url", files.store(file));
    }
}
