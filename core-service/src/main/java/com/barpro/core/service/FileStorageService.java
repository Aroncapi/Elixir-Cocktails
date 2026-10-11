package com.barpro.core.service;

import com.barpro.core.error.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    public static final String PUBLIC_PREFIX = "/api/public/files/";
    public static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> EXTENSIONES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "gif", "image/gif");
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private final Path baseDir;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.baseDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void crearDirectorio() {
        try {
            Files.createDirectories(baseDir);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo crear el directorio de subidas: " + baseDir, ex);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes adjuntar una imagen");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("La imagen no puede pesar mas de 5 MB");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!TIPOS_PERMITIDOS.contains(contentType)) {
            throw new IllegalArgumentException("Formato no permitido: usa JPG, PNG, WEBP o GIF");
        }
        String extension = extensionDe(contentType, file.getOriginalFilename());
        String nombre = "img-" + UUID.randomUUID() + "." + extension;
        try {
            Files.copy(file.getInputStream(), baseDir.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar la imagen", ex);
        }
        return PUBLIC_PREFIX + nombre;
    }

    public Path resolve(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty() || limpio.contains("..") || limpio.contains("/") || limpio.contains("\\")) {
            throw new NotFoundException("Archivo no encontrado");
        }
        Path resuelto = baseDir.resolve(limpio).normalize();
        if (!resuelto.startsWith(baseDir)) {
            throw new NotFoundException("Archivo no encontrado");
        }
        if (!Files.isRegularFile(resuelto)) {
            throw new NotFoundException("Archivo no encontrado: " + nombre);
        }
        return resuelto;
    }

    public void deleteIfLocal(String url) {
        if (url == null || !url.startsWith(PUBLIC_PREFIX)) {
            return;
        }
        String nombre = url.substring(PUBLIC_PREFIX.length());
        try {
            resolve(nombre);
        } catch (NotFoundException ex) {
            return;
        }
        try {
            Files.deleteIfExists(baseDir.resolve(nombre));
        } catch (IOException ex) {
            // mejor dejar un archivo huerfano que romper la operacion del catalogo
        }
    }

    public String contentTypeDe(Path archivo) {
        String nombre = archivo.getFileName().toString().toLowerCase(Locale.ROOT);
        int punto = nombre.lastIndexOf('.');
        String extension = punto >= 0 ? nombre.substring(punto + 1) : "";
        return EXTENSIONES.getOrDefault(extension, "application/octet-stream");
    }

    private String extensionDe(String contentType, String originalFilename) {
        String porTipo = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> null;
        };
        if (porTipo != null) {
            return porTipo;
        }
        String nombre = originalFilename == null ? "" : originalFilename.toLowerCase(Locale.ROOT);
        int punto = nombre.lastIndexOf('.');
        return punto >= 0 ? nombre.substring(punto + 1) : "bin";
    }
}
