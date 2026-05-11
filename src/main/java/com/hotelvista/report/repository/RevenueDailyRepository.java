package com.hotelvista.report.repository;

import com.hotelvista.report.model.RevenueDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RevenueDailyRepository extends JpaRepository<RevenueDaily, LocalDate> {
    Optional<RevenueDaily> findByDate(LocalDate date);
}
