package com.gdc.backend.treatment.dto;

import java.time.Instant;

public record ScanResponse(
        Long id,
        String originalFilename,
        String contentType,
        Long fileSizeBytes,
        Instant createdAt
) {
}
