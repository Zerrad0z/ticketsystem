package com.ticketsystem.backend.services;

import com.ticketsystem.backend.dtos.AuditLogDTO;
import com.ticketsystem.backend.dtos.TicketDTO;
import com.ticketsystem.backend.enums.Status;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TicketService {
    @Transactional
    TicketDTO createTicket(TicketDTO ticketDTO, Long userId);

    @Transactional
    TicketDTO updateStatus(Long ticketId, Status newStatus, Long userId);

    @Transactional
    TicketDTO addComment(Long ticketId, String content, Long userId);

    @Transactional(readOnly = true)
    List<TicketDTO> getUserTickets(Long userId);

    @Transactional(readOnly = true)
    List<TicketDTO> getAllTickets(Long userId);

    @Transactional(readOnly = true)
    List<TicketDTO> getTicketsByStatus(Status status, Long userId);

    @Transactional(readOnly = true)
    TicketDTO getTicketById(Long ticketId, Long userId);

    @Transactional(readOnly = true)
    List<AuditLogDTO> getAuditLogs(Long userId);
}
