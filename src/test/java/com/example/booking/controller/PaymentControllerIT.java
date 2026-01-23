package com.example.booking.controller;

import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.entity.AccommodationType;
import com.example.booking.entity.Unit;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private UUID unitId;
    private UUID userId;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("booking")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeEach
    void setUp() throws SQLException {
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        unitRepository.deleteAll();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users (id, email) VALUES (?, ?) ON CONFLICT DO NOTHING"
             )
        ) {
            ps.setObject(1, UUID.randomUUID());
            ps.setString(2, "test@example.com");
            ps.executeUpdate();
        }

        userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users LIMIT 1", (rs, rowNum) -> UUID.fromString(rs.getString("id"))
        );

        Unit unit = new Unit();
        unit.setRooms(2);
        unit.setType(AccommodationType.FLAT);
        unit.setFloor(1);
        unit.setBaseCost(BigDecimal.valueOf(100));
        unit.setDescription("Test Unit");
        unit = unitRepository.save(unit);
        this.unitId = unit.getId();
    }

    @Test
    void payBooking_success() throws Exception {
        CreateBookingRequest bookingRequest = new CreateBookingRequest(
                unitId, userId, LocalDate.now(), LocalDate.now().plusDays(1)
        );

        String bookingJson = """
            {
                "unitId":"%s",
                "userId":"%s",
                "startDate":"%s",
                "endDate":"%s"
            }
            """.formatted(
                bookingRequest.unitId(),
                bookingRequest.userId(),
                bookingRequest.startDate(),
                bookingRequest.endDate()
        );

        String bookingResponse = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String bookingId = com.jayway.jsonpath.JsonPath.read(bookingResponse, "$.bookingId");

        mockMvc.perform(post("/" + bookingId + "/pay"))
                .andExpect(status().isOk());
    }
}
