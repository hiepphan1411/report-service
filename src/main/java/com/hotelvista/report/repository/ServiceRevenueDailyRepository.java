package com.hotelvista.report.repository;

import com.hotelvista.report.model.ServiceRevenueDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ServiceRevenueDailyRepository extends JpaRepository<ServiceRevenueDaily, Long> {
    Optional<ServiceRevenueDaily> findByDateAndServiceId(LocalDate date, String serviceId);

    List<ServiceRevenueDaily> findByDate(LocalDate date);

    List<ServiceRevenueDaily> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
}
