package com.hotelvista.report.repository;

import com.hotelvista.report.model.DashboardSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DashboardSummaryRepository extends JpaRepository<DashboardSummary, Long> {
    Optional<DashboardSummary> findByMonthAndYear(Integer month, Integer year);
}
