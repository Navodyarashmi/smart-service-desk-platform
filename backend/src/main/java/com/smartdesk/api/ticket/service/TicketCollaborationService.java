package com.smartdesk.api.ticket.service;

import com.smartdesk.api.identity.model.Notification;
import com.smartdesk.api.identity.model.UserAccount;
import com.smartdesk.api.identity.repository.NotificationRepository;
import com.smartdesk.api.identity.repository.UserAccountRepository;
import com.smartdesk.api.ticket.model.ServiceTicket;
import com.smartdesk.api.ticket.model.TicketActivity;
import com.smartdesk.api.ticket.model.TicketActivityType;
import com.smartdesk.api.ticket.repository.ServiceTicketRepository;
import com.smartdesk.api.ticket.repository.TicketActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TicketCollaborationService {
    private final ServiceTicketRepository ticketRepository;
    private final TicketActivityRepository activityRepository;
    private final UserAccountRepository userRepository;
    private final NotificationRepository notificationRepository;

    public TicketCollaborationService(ServiceTicketRepository ticketRepository, TicketActivityRepository activityRepository, UserAccountRepository userRepository, NotificationRepository notificationRepository) {
        this.ticketRepository = ticketRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketActivityView> list(UUID ticketId, UUID actorId, boolean staff) {
        ServiceTicket ticket = accessibleTicket(ticketId, actorId, staff);
        return activityRepository.findAllByTicket_IdOrderByCreatedAtAsc(ticket.getId())
                .stream()
                .filter(activity -> staff || !activity.isInternalNote())
                .map(TicketCollaborationService::toView)
                .toList();
    }

    @Transactional
    public TicketActivityView comment(UUID ticketId, UUID actorId, boolean staff, String message, boolean internalNote) {
        if (internalNote && !staff) {
            throw new TicketStateConflictException("Only service desk staff can add internal notes.");
        }
        ServiceTicket ticket = accessibleTicket(ticketId, actorId, staff);
        UserAccount actor = userRepository.findById(actorId).orElseThrow(TicketRequesterNotFoundException::new);
        TicketActivity activity = activityRepository.saveAndFlush(TicketActivity.record(
                ticket,
                actor,
                internalNote ? TicketActivityType.INTERNAL_NOTE : TicketActivityType.COMMENT,
                message,
                internalNote
        ));

        if (!internalNote) {
            UserAccount recipient = staff ? ticket.getRequester() : ticket.getAssignee();
            if (recipient != null && !recipient.getId().equals(actorId)) {
                notificationRepository.save(Notification.create(
                        recipient,
                        ticket,
                        actor.getFullName() + " commented on " + ticket.getReferenceCode()
                ));
            }
        }
        return toView(activity);
    }

    @Transactional
    public void audit(ServiceTicket ticket, UserAccount actor, TicketActivityType type, String message) {
        activityRepository.save(TicketActivity.record(ticket, actor, type, message, false));
        UserAccount requester = ticket.getRequester();
        if (actor != null && !requester.getId().equals(actor.getId())) {
            notificationRepository.save(Notification.create(requester, ticket, message));
        }
    }

    private ServiceTicket accessibleTicket(UUID ticketId, UUID actorId, boolean staff) {
        ServiceTicket ticket = ticketRepository.findById(ticketId).orElseThrow(TicketNotFoundException::new);
        if (!staff && !ticket.getRequester().getId().equals(actorId)) throw new TicketNotFoundException();
        return ticket;
    }

    private static TicketActivityView toView(TicketActivity activity) {
        UserAccount actor = activity.getActor();
        return new TicketActivityView(
                activity.getId(), activity.getActivityType(), activity.getMessage(), activity.isInternalNote(),
                actor == null ? null : actor.getId(), actor == null ? "System" : actor.getFullName(), activity.getCreatedAt()
        );
    }
}
