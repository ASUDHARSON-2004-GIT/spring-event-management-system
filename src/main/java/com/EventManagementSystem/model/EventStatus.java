package com.EventManagementSystem.model;

import java.time.LocalDate;

public enum EventStatus {
    UPCOMING,
    ONGOING,
    COMPLETED,
    CANCELLED;

    public static EventStatus deriveStatus(LocalDate eventDate, EventStatus currentStatus) {

        if (currentStatus == CANCELLED) {
            return CANCELLED;
        }

        if (eventDate == null) {
            return currentStatus;
        }

        LocalDate today = LocalDate.now();

        if (eventDate.isEqual(today)) {
            return ONGOING;
        } else if (eventDate.isBefore(today)) {
            return COMPLETED;
        } else {
            return UPCOMING;
        }
    }
}