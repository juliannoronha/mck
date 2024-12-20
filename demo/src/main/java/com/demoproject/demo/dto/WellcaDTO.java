package com.demoproject.demo.dto;

import com.demoproject.demo.entity.Wellca.ServiceType;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WellcaDTO {

    private Long id;

    /* Delivery Tracking (Counts) */
    @NotNull(message = "Date is required")
    private LocalDate date;

    @PositiveOrZero(message = "Purolator deliveries must be zero or positive")
    @Max(value = 9999, message = "Number of deliveries cannot exceed 9999")
    private Integer purolator;

    @PositiveOrZero(message = "FedEx deliveries must be zero or positive")
    @Max(value = 9999, message = "Number of deliveries cannot exceed 9999")
    private Integer fedex;

    @PositiveOrZero(message = "1Courier deliveries must be zero or positive")
    @Max(value = 9999, message = "Number of deliveries cannot exceed 9999")
    private Integer oneCourier;

    @PositiveOrZero(message = "GoBolt deliveries must be zero or positive")
    @Max(value = 9999, message = "Number of deliveries cannot exceed 9999")
    private Integer goBolt;

    /* RX Sales Summary */
    @PositiveOrZero(message = "New RX count must be zero or positive")
    private Integer newRx;

    @PositiveOrZero(message = "Refill count must be zero or positive")
    private Integer refill;

    @PositiveOrZero(message = "ReAuth count must be zero or positive")
    private Integer reAuth;

    @PositiveOrZero(message = "Hold count must be zero or positive")
    private Integer hold;

    /* Weekly Profiles */
    @PositiveOrZero(message = "Profiles entered must be zero or positive")
    private Integer profilesEntered;

    @PositiveOrZero(message = "Who filled RX count must be zero or positive")
    private Integer whoFilledRx;

    @DecimalMin(value = "0.0", message = "Active percentage must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Active percentage must be between 0 and 100")
    private BigDecimal activePercentage;

    /* Professional Services */
    private ServiceType serviceType;

    @PositiveOrZero(message = "Service cost must be zero or positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid currency format")
    private BigDecimal serviceCost;
}
