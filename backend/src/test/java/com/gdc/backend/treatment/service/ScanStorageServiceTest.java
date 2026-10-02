package com.gdc.backend.treatment.service;

import com.gdc.backend.treatment.entity.ClinicalScan;
import com.gdc.backend.treatment.exception.TreatmentValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScanStorageServiceTest {

    @TempDir
    Path scanDir;

    @Test
    void rejectsFakeImageRenamedAsJpeg() {
        ScanStorageService service = new ScanStorageService(scanDir.toString());

        MockMultipartFile fakeJpeg = new MockMultipartFile(
                "scans",
                "scan.jpg",
                "image/jpeg",
                "not an image".getBytes()
        );

        assertThatThrownBy(() -> service.store(fakeJpeg))
                .isInstanceOf(TreatmentValidationException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void acceptsValidJpeg() throws IOException {
        assertValidImage(
                "scan.jpg",
                "image/jpeg",
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00},
                ".jpg"
        );
    }

    @Test
    void acceptsValidPng() throws IOException {
        assertValidImage(
                "scan.png",
                "image/png",
                new byte[] {
                        (byte) 0x89, 0x50, 0x4E, 0x47,
                        0x0D, 0x0A, 0x1A, 0x0A
                },
                ".png"
        );
    }

    @Test
    void acceptsValidWebp() throws IOException {
        assertValidImage(
                "scan.webp",
                "image/webp",
                new byte[] {
                        0x52, 0x49, 0x46, 0x46,
                        0x00, 0x00, 0x00, 0x00,
                        0x57, 0x45, 0x42, 0x50
                },
                ".webp"
        );
    }

    private void assertValidImage(
            String originalFilename,
            String contentType,
            byte[] content,
            String expectedExtension
    ) throws IOException {
        ScanStorageService service = new ScanStorageService(scanDir.toString());

        ClinicalScan scan = service.store(new MockMultipartFile(
                "scans",
                originalFilename,
                contentType,
                content
        ));

        assertThat(scan.getStoredFilename()).endsWith(expectedExtension);
        assertThat(Files.exists(scanDir.resolve(scan.getStoredFilename()))).isTrue();
    }
}
