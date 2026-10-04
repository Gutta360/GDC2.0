package com.gdc.backend.treatment.service;

import com.gdc.backend.treatment.entity.ClinicalScan;
import com.gdc.backend.treatment.exception.TreatmentValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

@Service
public class ScanStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScanStorageService.class);

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Path storageRoot;

    public ScanStorageService(
            @Value("${gdc.scan-storage-dir:${user.home}/gdc-scans}") String storageDir
    ) {
        this.storageRoot = Path.of(storageDir).toAbsolutePath().normalize();
    }

    public ClinicalScan store(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new TreatmentValidationException("Scan file cannot be empty");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new TreatmentValidationException("Only JPEG, PNG and WEBP scan images are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new TreatmentValidationException("Scan file must be 5 MB or smaller");
        }

        if (!contentType.equals(detectContentType(file))) {
            throw new TreatmentValidationException("Scan file content does not match its declared image type");
        }

        try {
            Files.createDirectories(storageRoot);

            String extension = extensionFor(contentType);
            String storedFilename = UUID.randomUUID() + extension;
            Path target = storageRoot.resolve(storedFilename).normalize();

            if (!target.startsWith(storageRoot)) {
                throw new TreatmentValidationException("Invalid scan filename");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            ClinicalScan scan = new ClinicalScan();
            scan.setStoredFilename(storedFilename);
            scan.setOriginalFilename(safeOriginalFilename(file.getOriginalFilename()));
            scan.setContentType(contentType);
            scan.setFileSizeBytes(file.getSize());
            return scan;
        } catch (IOException exception) {
            throw new TreatmentValidationException("Could not store scan file");
        }
    }

    void deleteStoredFile(ClinicalScan scan) {
        if (scan == null || scan.getStoredFilename() == null || scan.getStoredFilename().isBlank()) {
            return;
        }

        try {
            Path target = storageRoot.resolve(scan.getStoredFilename()).normalize();

            if (!target.startsWith(storageRoot)) {
                LOGGER.warn("Refusing to delete scan outside storage directory: {}", scan.getStoredFilename());
                return;
            }

            Files.deleteIfExists(target);
        } catch (IOException exception) {
            LOGGER.warn("Failed to delete stored scan file {}", scan.getStoredFilename(), exception);
        }
    }

    public Resource load(ClinicalScan scan) {
        if (scan == null || scan.getStoredFilename() == null || scan.getStoredFilename().isBlank()) {
            throw new TreatmentValidationException("Invalid scan file");
        }

        try {
            Path target = storageRoot.resolve(scan.getStoredFilename()).normalize();

            if (!target.startsWith(storageRoot)) {
                throw new TreatmentValidationException("Invalid scan filename");
            }

            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new TreatmentValidationException("Scan file is not available");
            }

            return resource;
        } catch (MalformedURLException exception) {
            throw new TreatmentValidationException("Could not read scan file");
        }
    }

    private String detectContentType(MultipartFile file) {
        byte[] header = readHeader(file);

        if (isJpeg(header)) {
            return "image/jpeg";
        }

        if (isPng(header)) {
            return "image/png";
        }

        if (isWebp(header)) {
            return "image/webp";
        }

        return "";
    }

    private byte[] readHeader(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             ByteArrayOutputStream header = new ByteArrayOutputStream()) {
            inputStream.transferTo(header);
            byte[] bytes = header.toByteArray();
            return bytes.length > 12
                    ? Arrays.copyOf(bytes, 12)
                    : bytes;
        } catch (IOException exception) {
            throw new TreatmentValidationException("Could not read scan file");
        }
    }

    private boolean isJpeg(byte[] header) {
        return header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] header) {
        byte[] pngSignature = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
        };
        return header.length >= pngSignature.length
                && Arrays.equals(Arrays.copyOf(header, pngSignature.length), pngSignature);
    }

    private boolean isWebp(byte[] header) {
        return header.length >= 12
                && header[0] == 'R'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == 'F'
                && header[8] == 'W'
                && header[9] == 'E'
                && header[10] == 'B'
                && header[11] == 'P';
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }

    private String safeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "scan";
        }

        String filename = Path.of(originalFilename).getFileName().toString();
        return filename.length() > 255
                ? filename.substring(0, 255)
                : filename;
    }
}
