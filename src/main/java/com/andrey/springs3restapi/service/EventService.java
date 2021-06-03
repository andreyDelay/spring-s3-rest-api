package com.andrey.springs3restapi.service;

import com.andrey.springs3restapi.model.Event;
import com.andrey.springs3restapi.model.User;

public interface EventService {

    void save(Event event);

    Event findById(Long id, User user);

}
