package com.example.booking.controller;

import com.example.booking.dto.CreateUnitRequest;
import com.example.booking.entity.AccommodationType;
import com.example.booking.entity.Unit;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class UnitControllerIT {

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
    private UnitRepository unitRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        unitRepository.deleteAll();
        redisTemplate.delete("units:available:today");
    }

    @Test
    void createUnit_respondsWithStatusOk() throws Exception {
        CreateUnitRequest request = new CreateUnitRequest(
                2,
                AccommodationType.FLAT.name(),
                1,
                BigDecimal.valueOf(100),
                "Nice studio"
        );

        mockMvc.perform(post("/api/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.rooms").value(2));
    }

    @Test
    void searchAvailable_returnsUnit() throws Exception {
        var unit = unitRepository.save(
                new Unit(
                        null,
                        2,
                        AccommodationType.FLAT,
                        1,
                        BigDecimal.valueOf(100),
                        "Nice studio",
                        List.of()
                ));

        mockMvc.perform(get("/api/units/search")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(unit.getId().toString()));
    }

    @Test
    void availableCount_returnsCountOne() throws Exception {
        CreateUnitRequest request = new CreateUnitRequest(
                2,
                AccommodationType.FLAT.name(),
                1,
                BigDecimal.valueOf(100),
                "Nice studio"
        );

        mockMvc.perform(post("/api/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/units/available/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }
}
