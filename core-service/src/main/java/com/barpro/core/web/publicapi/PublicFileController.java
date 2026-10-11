package com.barpro.core.web.publicapi;

import com.barpro.core.service.FileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
public class PublicFileController {

    private final FileStorageService files;

    public PublicFileController(FileStorageService files) {
        this.files = files;
    }

    @GetMapping("/api/public/files/{filename}")
    public ResponseEntity<Resource> get(@PathVariable String filename) {
        Path archivo = files.resolve(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(files.contentTypeDe(archivo)))
                .body(new FileSystemResource(archivo));
    }
}
