package com.hotelvista.report.service;

import com.hotelvista.report.dto.*;
import com.hotelvista.report.model.DashboardSummary;
import com.hotelvista.report.model.RevenueDaily;
import com.hotelvista.report.model.RoomStatistics;
import com.hotelvista.report.repository.DashboardSummaryRepository;
import com.hotelvista.report.repository.RevenueDailyRepository;
import com.hotelvista.report.repository.RoomStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportQueryService {

    private static final int TOTAL_ROOMS_FALLBACK = 50;

    private final RevenueDailyRepository revenueDailyRepository;
    private final RoomStatisticsRepository roomStatisticsRepository;
    private final DashboardSummaryRepository dashboardSummaryRepository;

    public DashboardStatsDto getDashboardStats(Integer month, Integer year) {
        LocalDate now = LocalDate.now();
        int m = month != null ? month : now.getMonthValue();
        int y = year != null ? year : now.getYear();
        YearMonth current = YearMonth.of(y, m);
        YearMonth previous = current.minusMonths(1);

        List<RevenueDaily> currentRows = revenueDailyRepository.findByDateBetweenOrderByDateAsc(
                current.atDay(1), current.atEndOfMonth());
        List<RevenueDaily> previousRows = revenueDailyRepository.findByDateBetweenOrderByDateAsc(
                previous.atDay(1), previous.atEndOfMonth());

        DashboardSummary currentSummary = dashboardSummaryRepository.findByMonthAndYear(m, y)
                .orElse(new DashboardSummary(null, m, y, 0, 0, 0.0));
        DashboardSummary previousSummary = dashboardSummaryRepository.findByMonthAndYear(
                        previous.getMonthValue(), previous.getYear())
                .orElse(new DashboardSummary(null, previous.getMonthValue(), previous.getYear(), 0, 0, 0.0));

        DashboardStatsDto dto = new DashboardStatsDto();
        double totalRevenue = currentRows.stream().mapToDouble(this::effectiveTotalRevenue).sum();
        double previousRevenue = previousRows.stream().mapToDouble(this::effectiveTotalRevenue).sum();
        dto.setTotalRevenue(totalRevenue);
        dto.setRevenueChange(percentChange(totalRevenue, previousRevenue));
        dto.setTotalBookings(currentSummary.getTotalBookings());
        dto.setBookingsChange(percentChange(currentSummary.getTotalBookings(), previousSummary.getTotalBookings()));
        dto.setTotalGuests(currentSummary.getTotalGuestsCheckin());
        dto.setGuestsChange(percentChange(currentSummary.getTotalGuestsCheckin(), previousSummary.getTotalGuestsCheckin()));
        dto.setBookedRooms(currentSummary.getTotalBookings());
        dto.setAvailableRooms(Math.max(TOTAL_ROOMS_FALLBACK - currentSummary.getTotalBookings(), 0));
        dto.setMaintenanceRooms(0);
        dto.setCleaningRooms(0);
        dto.setOccupancyRate(TOTAL_ROOMS_FALLBACK == 0 ? 0 : currentSummary.getTotalBookings() * 100.0 / TOTAL_ROOMS_FALLBACK);
        dto.setOccupancyChange(percentChange(dto.getOccupancyRate(),
                TOTAL_ROOMS_FALLBACK == 0 ? 0 : previousSummary.getTotalBookings() * 100.0 / TOTAL_ROOMS_FALLBACK));
        dto.setAvgRating(0);
        dto.setTotalReviews(0);
        dto.setPendingCheckIns(0);
        dto.setPendingCheckOuts(0);
        dto.setRevenueData(getLastSixMonthsRevenue(current));
        dto.setRoomTypeData(List.of(new NameCountDto("Rooms", currentSummary.getTotalBookings())));
        int checkedOutBookings = currentRows.stream().mapToInt(this::bookingCount).sum();
        dto.setBookingStatusData(List.of(
                new StatusCountDto("CHECKED_OUT", checkedOutBookings),
                new StatusCountDto("PENDING", Math.max(currentSummary.getTotalBookings() - checkedOutBookings, 0)),
                new StatusCountDto("CANCELLED", currentRows.stream().mapToInt(this::cancelledOrders).sum())
        ));
        dto.setDailyOccupancy(currentRows.stream()
                .map(row -> new DailyOccupancyDto(String.valueOf(row.getDate().getDayOfMonth()),
                        TOTAL_ROOMS_FALLBACK == 0 ? 0 : bookingCount(row) * 100.0 / TOTAL_ROOMS_FALLBACK))
                .toList());
        dto.setPopularServices(List.of(new PopularServiceDto("Hotel services",
                currentRows.stream().mapToInt(this::totalOrders).sum(),
                currentRows.stream().mapToDouble(this::safeServiceRevenue).sum())));
        return dto;
    }

    public RevenueDaily getDailyRevenue(LocalDate date) {
        return revenueDailyRepository.findByDate(date).orElse(new RevenueDaily(date));
    }

    public List<RoomStatistics> getTopRooms(Integer month, Integer year) {
        LocalDate now = LocalDate.now();
        int m = month != null ? month : now.getMonthValue();
        int y = year != null ? year : now.getYear();
        return roomStatisticsRepository.findByMonthAndYear(m, y).stream()
                .sorted(Comparator.comparing(RoomStatistics::getRevenue).reversed())
                .limit(10)
                .toList();
    }

    public List<RevenueReportDto> getRevenueByDateRange(LocalDate fromDate, LocalDate toDate) {
        return revenueDailyRepository.findByDateBetweenOrderByDateAsc(fromDate, toDate)
                .stream()
                .map(this::toDailyRevenueDto)
                .toList();
    }

    public List<RevenueReportDto> getDailyCurrentMonth() {
        YearMonth month = YearMonth.now();
        return getRevenueByDateRange(month.atDay(1), month.atEndOfMonth());
    }

    public List<RevenueReportDto> getWeeklyCurrentMonth() {
        YearMonth month = YearMonth.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return aggregateRevenue(month.atDay(1), month.atEndOfMonth(), row -> row.getDate().get(weekFields.weekOfMonth()),
                (week, rows) -> {
                    RevenueReportDto dto = aggregateRows(rows);
                    dto.setWeek(week);
                    dto.setYear(month.getYear());
                    dto.setMonth(month.getMonthValue());
                    dto.setLabel("Week " + week);
                    return dto;
                });
    }

    public List<RevenueReportDto> getMonthlyInYear(int year) {
        return aggregateRevenue(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31),
                row -> row.getDate().getMonthValue(),
                (month, rows) -> {
                    RevenueReportDto dto = aggregateRows(rows);
                    dto.setYear(year);
                    dto.setMonth(month);
                    dto.setLabel(YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)));
                    return dto;
                });
    }

    public List<RevenueReportDto> getQuarterlyInYear(int year) {
        return aggregateRevenue(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31),
                row -> ((row.getDate().getMonthValue() - 1) / 3) + 1,
                (quarter, rows) -> {
                    RevenueReportDto dto = aggregateRows(rows);
                    dto.setYear(year);
                    dto.setQuarter(quarter);
                    dto.setLabel("Q" + quarter + " " + year);
                    return dto;
                });
    }

    public List<RevenueReportDto> getYearlyRevenue() {
        List<RevenueDaily> rows = revenueDailyRepository.findAll();
        return rows.stream()
                .collect(Collectors.groupingBy(row -> row.getDate().getYear()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    RevenueReportDto dto = aggregateRows(entry.getValue());
                    dto.setYear(entry.getKey());
                    dto.setLabel(String.valueOf(entry.getKey()));
                    return dto;
                })
                .toList();
    }

    public List<BookingReportDto> getBookingReport(LocalDate startDate, LocalDate endDate, String period) {
        return getRevenueByDateRange(startDate, endDate).stream()
                .map(row -> new BookingReportDto(
                        row.getLabel(),
                        bookingCountFromDto(row),
                        bookingCountFromDto(row),
                        0,
                        0,
                        bookingCountFromDto(row) == 0 ? 0 : row.getRoomRevenue() / bookingCountFromDto(row),
                        row.getRoomRevenue()))
                .toList();
    }

    public List<ServiceReportDto> getServiceReport(LocalDate startDate, LocalDate endDate, String period) {
        return revenueDailyRepository.findByDateBetweenOrderByDateAsc(startDate, endDate).stream()
                .map(row -> {
                    double serviceRevenue = safeServiceRevenue(row);
                    int orders = totalOrders(row);
                    return new ServiceReportDto(row.getDate().toString(), 0, 0, 0, 0, 0,
                            serviceRevenue, orders, orders == 0 ? 0 : serviceRevenue / orders);
                })
                .toList();
    }

    public List<RoomOccupancyReportDto> getRoomOccupancyReport(LocalDate startDate, LocalDate endDate, String period) {
        return revenueDailyRepository.findByDateBetweenOrderByDateAsc(startDate, endDate).stream()
                .map(row -> new RoomOccupancyReportDto(
                        row.getDate().toString(),
                        TOTAL_ROOMS_FALLBACK,
                        bookingCount(row),
                        TOTAL_ROOMS_FALLBACK == 0 ? 0 : bookingCount(row) * 100.0 / TOTAL_ROOMS_FALLBACK,
                        bookingCount(row) == 0 ? 0 : safeRoomRevenue(row) / bookingCount(row),
                        safeRoomRevenue(row)))
                .toList();
    }

    private List<DashboardRevenueDto> getLastSixMonthsRevenue(YearMonth current) {
        return java.util.stream.IntStream.rangeClosed(0, 5)
                .mapToObj(i -> current.minusMonths(5L - i))
                .map(month -> {
                    List<RevenueDaily> rows = revenueDailyRepository.findByDateBetweenOrderByDateAsc(
                            month.atDay(1), month.atEndOfMonth());
                    return new DashboardRevenueDto(
                            month.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)),
                            rows.stream().mapToDouble(this::effectiveTotalRevenue).sum(),
                            rows.stream().mapToInt(this::bookingCount).sum());
                })
                .toList();
    }

    private RevenueReportDto toDailyRevenueDto(RevenueDaily row) {
        LocalDate date = row.getDate();
        return new RevenueReportDto(
                date.toString(),
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                null,
                null,
                bookingCount(row),
                safeRoomRevenue(row),
                safeServiceRevenue(row),
                effectiveTotalRevenue(row));
    }

    private <T> List<RevenueReportDto> aggregateRevenue(LocalDate fromDate, LocalDate toDate,
                                                        java.util.function.Function<RevenueDaily, T> classifier,
                                                        java.util.function.BiFunction<T, List<RevenueDaily>, RevenueReportDto> mapper) {
        return revenueDailyRepository.findByDateBetweenOrderByDateAsc(fromDate, toDate)
                .stream()
                .collect(Collectors.groupingBy(classifier))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey((left, right) -> ((Comparable) left).compareTo(right)))
                .map(entry -> mapper.apply(entry.getKey(), entry.getValue()))
                .toList();
    }

    private RevenueReportDto aggregateRows(List<RevenueDaily> rows) {
        return new RevenueReportDto(null, null, null, null, null, null,
                rows.stream().mapToInt(this::bookingCount).sum(),
                rows.stream().mapToDouble(this::safeRoomRevenue).sum(),
                rows.stream().mapToDouble(this::safeServiceRevenue).sum(),
                rows.stream().mapToDouble(this::effectiveTotalRevenue).sum());
    }

    private double effectiveTotalRevenue(RevenueDaily row) {
        double splitTotal = safeRoomRevenue(row) + safeServiceRevenue(row);
        return splitTotal > 0 ? splitTotal : safe(row.getTotalRevenue());
    }

    private double safeRoomRevenue(RevenueDaily row) {
        return safe(row.getRoomRevenue());
    }

    private double safeServiceRevenue(RevenueDaily row) {
        return safe(row.getServiceRevenue());
    }

    private int bookingCount(RevenueDaily row) {
        return row.getBookingCount() != null ? row.getBookingCount() : 0;
    }

    private int totalOrders(RevenueDaily row) {
        return row.getTotalOrders() != null ? row.getTotalOrders() : 0;
    }

    private int cancelledOrders(RevenueDaily row) {
        return row.getCancelledOrders() != null ? row.getCancelledOrders() : 0;
    }

    private int bookingCountFromDto(RevenueReportDto row) {
        return row.getBookingCount() != null ? row.getBookingCount() : 0;
    }

    private double safe(Double value) {
        return value != null ? value : 0;
    }

    private double percentChange(double current, double previous) {
        if (previous == 0) {
            return current == 0 ? 0 : 100;
        }
        return Math.round(((current - previous) / previous) * 10000.0) / 100.0;
    }
}
