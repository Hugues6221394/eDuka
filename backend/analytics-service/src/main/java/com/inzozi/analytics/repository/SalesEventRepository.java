package com.inzozi.analytics.repository;

import com.inzozi.analytics.model.SalesEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesEventRepository extends JpaRepository<SalesEvent, String> {
    List<SalesEvent> findByBusinessId(String businessId);
    List<SalesEvent> findByVendorId(String vendorId);
    List<SalesEvent> findByEventTimeBetween(LocalDateTime start, LocalDateTime end);
    List<SalesEvent> findByEventTypeAndBusinessId(SalesEvent.EventType eventType, String businessId);
}
