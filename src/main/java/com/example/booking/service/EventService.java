package com.example.booking.service;

import com.example.booking.entity.EventType;

public interface EventService {

    void log(EventType type, String payload);
}
