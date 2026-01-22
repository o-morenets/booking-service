package com.example.booking.service;

import com.example.booking.entity.Event;
import com.example.booking.entity.EventType;
import com.example.booking.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public void log(EventType type, String payload) {
        Event event = new Event();
        event.setType(type);
        event.setPayload(payload);
        event.setCreatedAt(Instant.now());
        eventRepository.save(event);
    }
}
