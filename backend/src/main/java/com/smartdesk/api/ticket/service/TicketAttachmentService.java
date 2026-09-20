package com.smartdesk.api.ticket.service;

import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.ticket.model.ServiceTicket;
import com.smartdesk.api.ticket.model.TicketActivityType;
import com.smartdesk.api.ticket.model.TicketAttachment;
import com.smartdesk.api.ticket.repository.ServiceTicketRepository;
import com.smartdesk.api.ticket.repository.TicketAttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class TicketAttachmentService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "application/pdf", ".pdf",
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "text/plain", ".txt"
    );

    private final TicketAttachmentRepository attachmentRepository;
    private final ServiceTicketRepository ticketRepository;
    private final UserAccountRepository userRepository;
    private final TicketCollaborationService collaborationService;
    private final Path storageRoot;
    private final long maximumSize;

    public TicketAttachmentService(
            TicketAttachmentRepository attachmentRepository,
            ServiceTicketRepository ticketRepository,
            UserAccountRepository userRepository,
            TicketCollaborationService collaborationService,
            @Value("${app.attachments.directory:./data/attachments}") String directory,
            @Value("${app.attachments.max-size-bytes:5242880}") long maximumSize
    ) {
        this.attachmentRepository = attachmentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.collaborationService = collaborationService;
        this.storageRoot = Path.of(directory).toAbsolutePath().normalize();
        this.maximumSize = maximumSize;
    }

    @Transactional(readOnly = true)
    public List<TicketAttachmentView> list(
            UUID ticketId,
            UUID actorId,
            boolean staff
    ) {
        ServiceTicket ticket = accessibleTicket(ticketId, actorId, staff);
        return attachmentRepository
                .findAllByTicket_IdOrderByCreatedAtAsc(ticket.getId())
                .stream()
                .map(TicketAttachmentService::toView)
                .toList();
    }

    @Transactional
    public TicketAttachmentView upload(
            UUID ticketId,
            UUID actorId,
            boolean staff,
            MultipartFile file
    ) {
        ServiceTicket ticket = accessibleTicket(ticketId, actorId, staff);
        UserAccount uploader = userRepository.findById(actorId)
                .orElseThrow(TicketRequesterNotFoundException::new);
        ValidatedFile validated = validate(file);
        String storedFilename = UUID.randomUUID() + validated.extension();
        Path storedPath = resolveStoredPath(storedFilename);

        try {
            Files.createDirectories(storageRoot);
            Files.write(
                    storedPath,
                    validated.content(),
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );
        } catch (IOException exception) {
            throw new AttachmentStorageException(
                    "The attachment could not be stored.",
                    exception
            );
        }

        try {
            TicketAttachment attachment = attachmentRepository.saveAndFlush(
                    TicketAttachment.create(
                            ticket,
                            uploader,
                            validated.originalFilename(),
                            storedFilename,
                            validated.contentType(),
                            validated.content().length
                    )
            );
            collaborationService.audit(
                    ticket,
                    uploader,
                    TicketActivityType.ATTACHMENT_ADDED,
                    "Attachment added: " + validated.originalFilename()
            );
            return toView(attachment);
        } catch (RuntimeException exception) {
            deleteQuietly(storedPath);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public TicketAttachmentDownload download(
            UUID ticketId,
            UUID attachmentId,
            UUID actorId,
            boolean staff
    ) {
        accessibleTicket(ticketId, actorId, staff);
        TicketAttachment attachment = attachmentRepository
                .findByIdAndTicket_Id(attachmentId, ticketId)
                .orElseThrow(TicketNotFoundException::new);

        try {
            byte[] content = Files.readAllBytes(
                    resolveStoredPath(attachment.getStoredFilename())
            );
            return new TicketAttachmentDownload(
                    attachment.getOriginalFilename(),
                    attachment.getContentType(),
                    content
            );
        } catch (IOException exception) {
            throw new AttachmentStorageException(
                    "The attachment content is unavailable.",
                    exception
            );
        }
    }

    private ServiceTicket accessibleTicket(
            UUID ticketId,
            UUID actorId,
            boolean staff
    ) {
        ServiceTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        if (!staff && !ticket.getRequester().getId().equals(actorId)) {
            throw new TicketNotFoundException();
        }
        return ticket;
    }

    private ValidatedFile validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AttachmentValidationException("Choose a non-empty file.");
        }
        if (file.getSize() > maximumSize) {
            throw new AttachmentValidationException(
                    "Attachments must not exceed 5 MB."
            );
        }

        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = ALLOWED_TYPES.get(contentType);
        if (extension == null) {
            throw new AttachmentValidationException(
                    "Only PDF, PNG, JPEG, and plain-text files are supported."
            );
        }

        try {
            byte[] content = file.getBytes();
            verifySignature(contentType, content);
            return new ValidatedFile(
                    safeFilename(file.getOriginalFilename(), extension),
                    contentType,
                    extension,
                    content
            );
        } catch (IOException exception) {
            throw new AttachmentStorageException(
                    "The attachment could not be read.",
                    exception
            );
        }
    }

    private static void verifySignature(String contentType, byte[] content) {
        boolean valid = switch (contentType) {
            case "application/pdf" -> startsWith(content, "%PDF-".getBytes(StandardCharsets.US_ASCII));
            case "image/png" -> startsWith(content, new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47});
            case "image/jpeg" -> startsWith(content, new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff});
            case "text/plain" -> !containsNullByte(content);
            default -> false;
        };
        if (!valid) {
            throw new AttachmentValidationException(
                    "The file content does not match its declared type."
            );
        }
    }

    private static boolean startsWith(byte[] content, byte[] signature) {
        if (content.length < signature.length) return false;
        for (int index = 0; index < signature.length; index++) {
            if (content[index] != signature[index]) return false;
        }
        return true;
    }

    private static boolean containsNullByte(byte[] content) {
        for (byte value : content) {
            if (value == 0) return true;
        }
        return false;
    }

    private static String safeFilename(String original, String extension) {
        String fallback = "attachment" + extension;
        if (original == null || original.isBlank()) return fallback;
        String normalized = original.replace('\\', '/');
        String filename = normalized.substring(normalized.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}]", "")
                .trim();
        if (filename.isBlank()) return fallback;
        return filename.length() <= 255 ? filename : filename.substring(0, 255);
    }

    private Path resolveStoredPath(String storedFilename) {
        Path resolved = storageRoot.resolve(storedFilename).normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new AttachmentStorageException(
                    "The attachment storage path is invalid.",
                    null
            );
        }
        return resolved;
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // The database transaction still fails; orphan cleanup can be retried operationally.
        }
    }

    private static TicketAttachmentView toView(TicketAttachment attachment) {
        UserAccount uploader = attachment.getUploader();
        return new TicketAttachmentView(
                attachment.getId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                uploader == null ? null : uploader.getId(),
                uploader == null ? "Former user" : uploader.getFullName(),
                attachment.getCreatedAt()
        );
    }

    private record ValidatedFile(
            String originalFilename,
            String contentType,
            String extension,
            byte[] content
    ) {
    }
}
