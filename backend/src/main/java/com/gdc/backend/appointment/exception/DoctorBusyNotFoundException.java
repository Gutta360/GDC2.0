package com.gdc.backend.appointment.exception;

public class DoctorBusyNotFoundException extends RuntimeException {

    public DoctorBusyNotFoundException(Long id) {
        super("Doctor busy slot not found: " + id);
    }
}
