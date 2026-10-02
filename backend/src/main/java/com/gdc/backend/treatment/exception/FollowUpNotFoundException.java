package com.gdc.backend.treatment.exception;

public class FollowUpNotFoundException extends RuntimeException {
    public FollowUpNotFoundException(String followUpId) {
        super("Follow-up not found: " + followUpId);
    }
}
