package com.example.booking.service;

import com.example.booking.entity.Event;
import com.example.booking.entity.EventType;
import com.example.booking.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void log_shouldSaveEvent() {
        EventType type = EventType.BOOKING_CREATED;
        String payload = "Booking created, bookingId=123";

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        eventService.log(type, payload);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();

        assertEquals(type, savedEvent.getType());
        assertEquals(payload, savedEvent.getPayload());
        assertNotNull(savedEvent.getCreatedAt());

        assertTrue(savedEvent.getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
    }
}
