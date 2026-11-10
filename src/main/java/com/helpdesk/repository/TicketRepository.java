
package com.helpdesk.repository;

import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    List<Ticket> findByAssignedToOrderByCreatedAtDesc(User assignedTo);
}
