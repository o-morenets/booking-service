package com.example.booking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "booking.payment")
@Getter
@Setter
public class PaymentProperties {

    private BigDecimal markup;
}
