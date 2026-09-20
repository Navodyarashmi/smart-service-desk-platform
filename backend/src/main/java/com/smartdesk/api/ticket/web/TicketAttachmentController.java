package com.smartdesk.api.ticket.web;

import com.smartdesk.api.ticket.service.TicketAttachmentDownload;
import com.smartdesk.api.ticket.service.TicketAttachmentService;
import com.smartdesk.api.ticket.service.TicketAttachmentView;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/attachments")
public class TicketAttachmentController {

    private final TicketAttachmentService service;

    public TicketAttachmentController(TicketAttachmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<TicketAttachmentResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId
    ) {
        return service.list(ticketId, userId(jwt), isStaff(jwt))
                .stream()
                .map(TicketAttachmentController::response)
                .toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TicketAttachmentResponse upload(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @RequestParam("file") MultipartFile file
    ) {
        return response(service.upload(
                ticketId,
                userId(jwt),
                isStaff(jwt),
                file
        ));
    }

    @GetMapping("/{attachmentId}/content")
    public ResponseEntity<byte[]> download(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId,
            @PathVariable UUID attachmentId
    ) {
        TicketAttachmentDownload download = service.download(
                ticketId,
                attachmentId,
                userId(jwt),
                isStaff(jwt)
        );
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.content().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.filename(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(download.content());
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static boolean isStaff(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && (
                roles.contains("TECHNICIAN")
                        || roles.contains("ADMINISTRATOR")
        );
    }

    private static TicketAttachmentResponse response(
            TicketAttachmentView attachment
    ) {
        return new TicketAttachmentResponse(
                attachment.id(),
                attachment.filename(),
                attachment.contentType(),
                attachment.sizeBytes(),
                attachment.uploaderId(),
                attachment.uploaderName(),
                attachment.createdAt()
        );
    }
}
