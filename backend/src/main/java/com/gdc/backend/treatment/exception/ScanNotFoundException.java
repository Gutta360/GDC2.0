package com.gdc.backend.treatment.exception;

public class ScanNotFoundException extends RuntimeException {
    public ScanNotFoundException(Long scanId) {
        super("Scan not found: " + scanId);
    }
}
