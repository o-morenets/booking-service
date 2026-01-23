package com.example.booking.controller;

import com.example.booking.dto.BookingResponse;
import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.entity.AccommodationType;
import com.example.booking.entity.Unit;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.UnitRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
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

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class BookingControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("booking")
            .withUsername("user")
            .withPassword("pass");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private UUID unitId;
    private UUID userId;

    @Autowired
    private DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        bookingRepository.deleteAll();
        unitRepository.deleteAll();
        redisTemplate.delete("units:available:today");

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
        unit.setDescription("Nice studio");

        unit = unitRepository.save(unit);
        this.unitId = unit.getId();
    }

    @Test
    void createBooking_success() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest(
                unitId,
                userId,
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId", notNullValue()))
                .andExpect(jsonPath("$.unitId").value(unitId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void cancelBooking_success() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest(
                unitId,
                userId,
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );

        String responseJson = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        BookingResponse booking = objectMapper.readValue(responseJson, BookingResponse.class);

        mockMvc.perform(post("/api/bookings/" + booking.bookingId() + "/cancel"))
                .andExpect(status().isOk());
    }
}
