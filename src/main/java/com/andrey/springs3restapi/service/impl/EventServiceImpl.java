package com.andrey.springs3restapi.service.impl;

import com.andrey.springs3restapi.model.Event;
import com.andrey.springs3restapi.model.User;
import com.andrey.springs3restapi.repository.EventRepository;
import com.andrey.springs3restapi.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void save(Event event) {
        eventRepository.save(event);
    }

    @Override
    public Event findById(Long id, User user) {
        return eventRepository.findByIdAndUser(id, user);
        /*return eventRepository.findById(id).orElse(null);*/
    }

}
