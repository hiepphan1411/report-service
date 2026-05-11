package com.hotelvista.report.repository;

import com.hotelvista.report.model.RoomStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomStatisticsRepository extends JpaRepository<RoomStatistics, Long> {
    Optional<RoomStatistics> findByRoomIdAndMonthAndYear(Long roomId, Integer month, Integer year);
}
