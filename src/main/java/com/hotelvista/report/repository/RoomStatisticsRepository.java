package com.hotelvista.report.repository;

import com.hotelvista.report.model.RoomStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoomStatisticsRepository extends JpaRepository<RoomStatistics, Long> {
    Optional<RoomStatistics> findByRoomIdAndMonthAndYear(Long roomId, Integer month, Integer year);
    List<RoomStatistics> findByMonthAndYear(Integer month, Integer year);
}
