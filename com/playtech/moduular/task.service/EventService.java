package com.playtech.assignment.moduular.task.service;

import com.playtech.assignment.moduular.task.model.Event;

import java.util.List;

public interface EventService {
    void logEvent(Event event);
    List<Event> getEvents();
}
