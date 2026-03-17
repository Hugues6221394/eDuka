package com.inzozi.analytics.repository;

import com.inzozi.analytics.model.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, String> {
    List<Commission> findByBusinessId(String businessId);
    List<Commission> findByVendorId(String vendorId);
    List<Commission> findByStatus(Commission.CommissionStatus status);
}
