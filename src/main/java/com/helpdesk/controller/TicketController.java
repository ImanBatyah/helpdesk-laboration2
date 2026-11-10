
package com.helpdesk.controller;

import com.helpdesk.model.Comment;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;
import com.helpdesk.repository.CommentRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class TicketController {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public TicketController(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            CommentRepository commentRepository) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/tickets")
    public String listTickets(
            Authentication authentication,
            Model model) {

        User currentUser = getCurrentUser(authentication);

        List<Ticket> tickets;

        if (currentUser.getRole() == User.Role.ADMIN) {
            tickets = ticketRepository
                    .findAllByOrderByCreatedAtDesc();

        } else if (currentUser.getRole() == User.Role.AGENT) {
            tickets = ticketRepository
                    .findByAssignedToOrderByCreatedAtDesc(currentUser);

        } else {
            tickets = ticketRepository
                    .findByCreatedByOrderByCreatedAtDesc(currentUser);
        }

        model.addAttribute("tickets", tickets);

        return "tickets/list";
    }

    @GetMapping("/tickets/new")
    public String showCreateTicketForm(Model model) {

        model.addAttribute("ticket", new Ticket());

        return "tickets/new";
    }

    @PostMapping("/tickets")
    public String createTicket(
            @Valid @ModelAttribute("ticket") Ticket ticket,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            return "tickets/new";
        }

        User currentUser = getCurrentUser(authentication);

        Ticket newTicket = new Ticket();

        newTicket.setTitle(ticket.getTitle());
        newTicket.setDescription(ticket.getDescription());
        newTicket.setPriority(ticket.getPriority());
        newTicket.setCreatedBy(currentUser);

        ticketRepository.save(newTicket);

        return "redirect:/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String showTicketDetails(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        User currentUser = getCurrentUser(authentication);
        Ticket ticket = getAuthorizedTicket(id, currentUser);

        List<Comment> comments = commentRepository
                .findByTicketOrderByCreatedAtAsc(ticket);

        model.addAttribute("ticket", ticket);
        model.addAttribute("comments", comments);

        boolean isAdmin =
                currentUser.getRole() == User.Role.ADMIN;

        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            model.addAttribute(
                    "agents",
                    userRepository.findByRoleAndEnabledTrue(
                            User.Role.AGENT)
            );
        }

        return "tickets/details";
    }

    @PostMapping("/tickets/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam("content") String content,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        Ticket ticket = getAuthorizedTicket(id, currentUser);

        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Kommentaren får inte vara tom");
        }

        String trimmedContent = content.trim();

        if (trimmedContent.length() > 5000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Kommentaren är för lång");
        }

        Comment comment = new Comment(
                trimmedContent,
                ticket,
                currentUser
        );

        commentRepository.save(comment);

        return "redirect:/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/status")
    public String updateTicketStatus(
            @PathVariable Long id,
            @RequestParam("status") Ticket.Status status,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN);
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND));

        ticket.setStatus(status);
        ticketRepository.save(ticket);

        return "redirect:/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/priority")
    public String updateTicketPriority(
            @PathVariable Long id,
            @RequestParam("priority") Ticket.Priority priority,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN);
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND));

        ticket.setPriority(priority);
        ticketRepository.save(ticket);

        return "redirect:/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/assign")
    public String assignTicket(
            @PathVariable Long id,
            @RequestParam("agentId") Long agentId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN);
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND));

        if (agent.getRole() != User.Role.AGENT
                || !agent.isEnabled()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Användaren är inte en aktiv supportmedarbetare");
        }

        ticket.setAssignedTo(agent);
        ticketRepository.save(ticket);

        return "redirect:/tickets/" + id;
    }

    private User getCurrentUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED));
    }

    private Ticket getAuthorizedTicket(
            Long id,
            User currentUser) {

        Ticket ticket = ticketRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND));

        boolean isAdmin =
                currentUser.getRole() == User.Role.ADMIN;

        boolean isCreator =
                ticket.getCreatedBy() != null
                && ticket.getCreatedBy().getId()
                        .equals(currentUser.getId());

        boolean isAssignedAgent =
                currentUser.getRole() == User.Role.AGENT
                && ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId()
                        .equals(currentUser.getId());

        if (!isAdmin && !isCreator && !isAssignedAgent) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN);
        }

        return ticket;
    }
}
