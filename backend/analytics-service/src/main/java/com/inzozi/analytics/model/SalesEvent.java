package com.inzozi.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String businessId;

    @Column(nullable = false)
    private String vendorId;

    private String productId;

    @Column(nullable = false)
    private String bookingId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 50)
    private String category;

    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
    }

    public enum EventType {
        BOOKING_CREATED,
        BOOKING_CONFIRMED,
        BOOKING_COMPLETED,
        BOOKING_CANCELLED
    }
}
