package com.andrey.springs3restapi.repository;

import com.andrey.springs3restapi.model.Event;
import com.andrey.springs3restapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findById(Long eventId);

    Event findByIdAndUser(Long id, User user);
}
