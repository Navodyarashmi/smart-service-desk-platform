package com.smartdesk.api.ticket.service;

public record TicketAttachmentDownload(
        String filename,
        String contentType,
        byte[] content
) {
}
