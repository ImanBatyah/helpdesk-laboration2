
package com.helpdesk.repository;

import com.helpdesk.model.Comment;
import com.helpdesk.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTicketOrderByCreatedAtAsc(Ticket ticket);

    long countByTicket(Ticket ticket);
}
