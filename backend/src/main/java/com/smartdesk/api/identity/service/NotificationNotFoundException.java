package com.smartdesk.api.identity.service;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException() {
        super("Notification was not found.");
    }
}
