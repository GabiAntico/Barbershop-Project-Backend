package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.dashboard.ClientDashboardResponse;
import com.barbershop.shifts.dtos.dashboard.DashboardResponse;
import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.entities.ShiftStatus;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.entities.Visit;
import com.barbershop.shifts.entities.VisitPaymentMovement;
import com.barbershop.shifts.entities.VisitPaymentMovementType;
import com.barbershop.shifts.repositories.ClientRepositoryJpa;
import com.barbershop.shifts.repositories.ShiftRepositoryJpa;
import com.barbershop.shifts.repositories.VisitRepositoryJpa;
import com.barbershop.shifts.services.CurrentUserService;
import com.barbershop.shifts.services.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ShiftRepositoryJpa shiftRepository;
    private final VisitRepositoryJpa visitRepository;
    private final ClientRepositoryJpa clientRepository;
    private final CurrentUserService currentUserService;

    public DashboardServiceImpl(
            ShiftRepositoryJpa shiftRepository,
            VisitRepositoryJpa visitRepository,
            ClientRepositoryJpa clientRepository,
            CurrentUserService currentUserService
    ) {
        this.shiftRepository = shiftRepository;
        this.visitRepository = visitRepository;
        this.clientRepository = clientRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public DashboardResponse getDashboard(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);

        Branch branch = currentUserService.getCurrentBranch();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<Shift> shifts = shiftRepository.findByDatetimeBetweenAndBranch(start, end, branch);
        List<Visit> visits = visitRepository.findByShiftDatetimeBetweenAndShiftBranch(start, end, branch);

        DashboardResponse response = new DashboardResponse();
        response.setRevenue(buildRevenueStats(visits));
        response.setAttendance(buildAttendanceStats(shifts, branch));

        return response;
    }

    @Override
    public ClientDashboardResponse getClientDashboard(Long clientId, YearMonth month) {
        if (clientId == null || clientId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client id is required");
        }

        if (month == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Month is required");
        }

        User owner = currentUserService.getCurrentUser();
        Branch branch = currentUserService.getCurrentBranch();
        Client client = clientRepository.findByIdAndBarbershop(clientId, owner.getBarbershop())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<Shift> shifts = shiftRepository.findByClientIdAndDatetimeBetweenAndBranch(clientId, start, end, branch);
        List<Visit> selectedMonthVisits = visitRepository.findByShiftClientIdAndShiftDatetimeBetweenAndShiftBranch(clientId, start, end, branch);
        List<Visit> historicalVisits = visitRepository.findByShiftClientIdAndShiftBranch(clientId, branch);

        ClientDashboardResponse response = new ClientDashboardResponse();
        response.setClient(buildClientStats(client));
        response.setAttendance(buildAttendanceStats(shifts, branch));
        response.setSelectedMonthRevenue(buildRevenueStats(selectedMonthVisits));
        response.setHistoricalRevenue(buildRevenueStats(historicalVisits));
        response.setVisitFrequency(buildVisitFrequencyStats(historicalVisits));

        return response;
    }

    private DashboardResponse.RevenueStats buildRevenueStats(List<Visit> visits) {
        List<Visit> visitsWithRevenue = visits.stream()
                .filter(visit -> getNetPaidAmount(visit).compareTo(BigDecimal.ZERO) > 0)
                .toList();

        BigDecimal total = visitsWithRevenue.stream()
                .map(this::getNetPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long count = visitsWithRevenue.size();
        BigDecimal averageTicket = count == 0
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        DashboardResponse.RevenueStats stats = new DashboardResponse.RevenueStats();
        stats.setTotalPaidAmount(total);
        stats.setPaidVisitsCount(count);
        stats.setAverageTicket(averageTicket);

        return stats;
    }

    private BigDecimal getNetPaidAmount(Visit visit) {
        BigDecimal grossPaid = sumMovements(visit, VisitPaymentMovementType.PAYMENT);
        BigDecimal refunded = sumMovements(visit, VisitPaymentMovementType.REFUND);
        BigDecimal netPaid = grossPaid.subtract(refunded);

        return netPaid.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : netPaid;
    }

    private BigDecimal sumMovements(Visit visit, VisitPaymentMovementType type) {
        return visit.getPaymentMovements().stream()
                .filter(movement -> movement.getType() == type)
                .map(VisitPaymentMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private DashboardResponse.AttendanceStats buildAttendanceStats(List<Shift> shifts, Branch branch) {
        LocalDateTime now = LocalDateTime.now(branch.resolveZoneId());

        long completed = shifts.stream().filter(shift -> shift.getStatus() == ShiftStatus.COMPLETED).count();
        long cancelled = shifts.stream().filter(shift -> shift.getStatus() == ShiftStatus.CANCELLED).count();
        long missed = shifts.stream()
                .filter(shift -> shift.getStatus() == ShiftStatus.PENDING)
                .filter(shift -> shift.getDatetime().isBefore(now))
                .count();
        long futurePending = shifts.stream()
                .filter(shift -> shift.getStatus() == ShiftStatus.PENDING)
                .filter(shift -> !shift.getDatetime().isBefore(now))
                .count();

        long totalEvaluated = completed + cancelled + missed;
        BigDecimal attendanceRate = totalEvaluated == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalEvaluated), 2, RoundingMode.HALF_UP);

        DashboardResponse.AttendanceStats stats = new DashboardResponse.AttendanceStats();
        stats.setCompleted(completed);
        stats.setCancelled(cancelled);
        stats.setMissed(missed);
        stats.setFuturePending(futurePending);
        stats.setTotalEvaluated(totalEvaluated);
        stats.setAttendanceRate(attendanceRate);

        return stats;
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date can't be before start date");
        }
    }

    private ClientDashboardResponse.ClientStats buildClientStats(Client client) {
        ClientDashboardResponse.ClientStats stats = new ClientDashboardResponse.ClientStats();
        stats.setId(client.getId());
        stats.setFirstName(client.getFirstName());
        stats.setLastName(client.getLastName());
        stats.setPhoneNumber(client.getPhoneNumber());
        stats.setEmail(client.getEmail());
        stats.setNotes(client.getNotes());

        return stats;
    }

    private ClientDashboardResponse.VisitFrequencyStats buildVisitFrequencyStats(List<Visit> visits) {
        List<LocalDateTime> dates = visits.stream()
                .map(visit -> visit.getShift().getDatetime())
                .sorted()
                .toList();

        ClientDashboardResponse.VisitFrequencyStats stats = new ClientDashboardResponse.VisitFrequencyStats();
        stats.setVisitsCount((long) dates.size());
        stats.setLastVisitAt(dates.stream().max(Comparator.naturalOrder()).orElse(null));

        if (dates.size() < 2) {
            stats.setAverageDaysBetweenVisits(null);
            return stats;
        }

        long totalDays = 0;
        for (int i = 1; i < dates.size(); i++) {
            long gapDays = Duration.between(dates.get(i - 1), dates.get(i)).toDays();
            totalDays += Math.max(gapDays, 1);
        }

        double average = (double) totalDays / (dates.size() - 1);
        stats.setAverageDaysBetweenVisits((int) Math.ceil(average));

        return stats;
    }
}
