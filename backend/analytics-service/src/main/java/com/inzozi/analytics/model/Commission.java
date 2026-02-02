package com.inzozi.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String bookingId;

    @Column(nullable = false)
    private String businessId;

    @Column(nullable = false)
    private String vendorId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal bookingAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate; // e.g., 5.00 for 5%

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private CommissionStatus status;

    private LocalDateTime calculatedAt;

    private LocalDateTime paidAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculatedAt = LocalDateTime.now();
        if (status == null) {
            status = CommissionStatus.PENDING;
        }
    }

    public enum CommissionStatus {
        PENDING,
        PAID,
        CANCELLED
    }
}
