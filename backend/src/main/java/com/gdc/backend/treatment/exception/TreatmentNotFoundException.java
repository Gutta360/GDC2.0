package com.gdc.backend.treatment.exception;

public class TreatmentNotFoundException extends RuntimeException {
    public TreatmentNotFoundException(String treatmentId) {
        super("Treatment not found: " + treatmentId);
    }
}
