package com.ticketsystem.backend.services;

import com.ticketsystem.backend.dtos.AuditLogDTO;
import com.ticketsystem.backend.dtos.TicketDTO;
import com.ticketsystem.backend.entities.Ticket;
import com.ticketsystem.backend.entities.User;
import com.ticketsystem.backend.enums.Category;
import com.ticketsystem.backend.enums.Priority;
import com.ticketsystem.backend.enums.Role;
import com.ticketsystem.backend.enums.Status;
import com.ticketsystem.backend.exceptions.InvalidTicketDataException;
import com.ticketsystem.backend.exceptions.TicketNotFoundException;
import com.ticketsystem.backend.exceptions.UnauthorizedAccessException;
import com.ticketsystem.backend.exceptions.UserNotFoundException;
import com.ticketsystem.backend.mappers.AuditLogMapper;
import com.ticketsystem.backend.mappers.CommentMapper;
import com.ticketsystem.backend.mappers.TicketMapper;
import com.ticketsystem.backend.repositories.TicketRepository;
import com.ticketsystem.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Added to resolve unnecessary stubbing
public class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User employee;
    private User itSupport;
    private Ticket ticket;
    private TicketDTO ticketDTO;

    @BeforeEach
    void setUp() {
        // Setup Employee user
        employee = new User();
        employee.setId(1L);
        employee.setUsername("employee");
        employee.setRole(Role.ROLE_EMPLOYEE);

        // Setup IT Support user
        itSupport = new User();
        itSupport.setId(2L);
        itSupport.setUsername("itsupport");
        itSupport.setRole(Role.ROLE_IT_SUPPORT);

        // Setup ticket
        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("Test Ticket");
        ticket.setDescription("Test Description");
        ticket.setPriority(Priority.MEDIUM);
        ticket.setCategory(Category.SOFTWARE);
        ticket.setStatus(Status.NEW);
        ticket.setCreatedBy(employee);
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setLastUpdated(LocalDateTime.now());
        ticket.setTicketComments(new ArrayList<>());
        ticket.setAuditLogs(new ArrayList<>());

        // Setup ticketDTO
        ticketDTO = new TicketDTO();
        ticketDTO.setId(1L);
        ticketDTO.setTitle("Test Ticket");
        ticketDTO.setDescription("Test Description");
        ticketDTO.setPriority(Priority.MEDIUM);
        ticketDTO.setCategory(Category.SOFTWARE);
        ticketDTO.setStatus(Status.NEW);
        ticketDTO.setCreatedById(employee.getId());
    }

    @Test
    void createTicket_WithValidData_ShouldReturnTicketDTO() {
        // Arrange
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(ticketMapper.toEntity(any(TicketDTO.class))).thenReturn(ticket);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

        // Act
        TicketDTO result = ticketService.createTicket(ticketDTO, employee.getId());

        // Assert
        assertNotNull(result);
        assertEquals(ticketDTO.getTitle(), result.getTitle());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicket_WithInvalidData_ShouldThrowException() {
        // Arrange
        TicketDTO invalidTicket = new TicketDTO();
        invalidTicket.setTitle(""); // Invalid title

        // Intentionally no stubs here to avoid unnecessary stubbing warning

        // Act & Assert
        assertThrows(InvalidTicketDataException.class, () -> {
            ticketService.createTicket(invalidTicket, employee.getId());
        });
    }

    @Test
    void createTicket_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            ticketService.createTicket(ticketDTO, 99L);
        });
    }

    @Test
    void updateStatus_WithITSupport_ShouldUpdateStatus() {
        // Arrange
        when(userRepository.findById(itSupport.getId())).thenReturn(Optional.of(itSupport));
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

        // Act
        TicketDTO result = ticketService.updateStatus(ticket.getId(), Status.IN_PROGRESS, itSupport.getId());

        // Assert
        assertNotNull(result);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void updateStatus_WithEmployee_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            ticketService.updateStatus(ticket.getId(), Status.IN_PROGRESS, employee.getId());
        });
    }

    @Test
    void getUserTickets_ShouldReturnUserTickets() {
        // Arrange
        List<Ticket> tickets = List.of(ticket);
        List<TicketDTO> ticketDTOs = List.of(ticketDTO);

        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(ticketRepository.findByCreatedBy_Id(employee.getId())).thenReturn(tickets);
        when(ticketMapper.toDTOList(tickets)).thenReturn(ticketDTOs);

        // Act
        List<TicketDTO> result = ticketService.getUserTickets(employee.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ticketDTO, result.get(0));
    }

    @Test
    void getAllTickets_WithITSupport_ShouldReturnAllTickets() {
        // Arrange
        List<Ticket> tickets = List.of(ticket);
        List<TicketDTO> ticketDTOs = List.of(ticketDTO);

        when(userRepository.findById(itSupport.getId())).thenReturn(Optional.of(itSupport));
        when(ticketRepository.findAll()).thenReturn(tickets);
        when(ticketMapper.toDTOList(tickets)).thenReturn(ticketDTOs);

        // Act
        List<TicketDTO> result = ticketService.getAllTickets(itSupport.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ticketDTO, result.get(0));
    }

    @Test
    void getAllTickets_WithEmployee_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            ticketService.getAllTickets(employee.getId());
        });
    }

    @Test
    void getTicketById_AsOwner_ShouldReturnTicket() {
        // Arrange
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

        // Act
        TicketDTO result = ticketService.getTicketById(ticket.getId(), employee.getId());

        // Assert
        assertNotNull(result);
        assertEquals(ticketDTO, result);
    }

    @Test
    void getTicketById_AsITSupport_ShouldReturnTicket() {
        // Arrange
        when(userRepository.findById(itSupport.getId())).thenReturn(Optional.of(itSupport));
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketMapper.toDTO(ticket)).thenReturn(ticketDTO);

        // Act
        TicketDTO result = ticketService.getTicketById(ticket.getId(), itSupport.getId());

        // Assert
        assertNotNull(result);
        assertEquals(ticketDTO, result);
    }

    @Test
    void getTicketById_AsOtherEmployee_ShouldThrowException() {
        // Arrange
        User otherEmployee = new User();
        otherEmployee.setId(3L);
        otherEmployee.setUsername("other");
        otherEmployee.setRole(Role.ROLE_EMPLOYEE);

        when(userRepository.findById(otherEmployee.getId())).thenReturn(Optional.of(otherEmployee));
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            ticketService.getTicketById(ticket.getId(), otherEmployee.getId());
        });
    }

    @Test
    void getTicketById_NonExistentTicket_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TicketNotFoundException.class, () -> {
            ticketService.getTicketById(99L, employee.getId());
        });
    }

    @Test
    void getTicketsByStatus_ShouldReturnFilteredTickets() {
        // Arrange
        List<Ticket> tickets = List.of(ticket);
        List<TicketDTO> ticketDTOs = List.of(ticketDTO);

        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(ticketRepository.findByStatus(Status.NEW)).thenReturn(tickets);
        when(ticketMapper.toDTOList(tickets)).thenReturn(ticketDTOs);

        // Act
        List<TicketDTO> result = ticketService.getTicketsByStatus(Status.NEW, employee.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ticketDTO, result.get(0));
    }

    @Test
    void addComment_AsITSupport_ShouldAddComment() {
        // Arrange
        String commentContent = "Test comment";

        when(userRepository.findById(itSupport.getId())).thenReturn(Optional.of(itSupport));
        when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toDTO(any(Ticket.class))).thenReturn(ticketDTO);

        // Act
        TicketDTO result = ticketService.addComment(ticket.getId(), commentContent, itSupport.getId());

        // Assert
        assertNotNull(result);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void addComment_AsEmployee_ShouldThrowException() {
        // Arrange
        String commentContent = "Test comment";

        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            ticketService.addComment(ticket.getId(), commentContent, employee.getId());
        });
    }

    @Test
    void addComment_WithEmptyContent_ShouldThrowException() {
        // Arrange
        String emptyComment = "";

        // Act & Assert
        assertThrows(InvalidTicketDataException.class, () -> {
            ticketService.addComment(ticket.getId(), emptyComment, itSupport.getId());
        });
    }

    @Test
    void getAuditLogs_AsITSupport_ShouldReturnLogs() {
        // Arrange
        when(userRepository.findById(itSupport.getId())).thenReturn(Optional.of(itSupport));
        when(ticketRepository.findAll()).thenReturn(List.of(ticket));
        when(auditLogMapper.toDTO(any())).thenReturn(new AuditLogDTO());

        // Act
        List<AuditLogDTO> result = ticketService.getAuditLogs(itSupport.getId());

        // Assert
        assertNotNull(result);
    }

    @Test
    void getAuditLogs_AsEmployee_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            ticketService.getAuditLogs(employee.getId());
        });
    }
}