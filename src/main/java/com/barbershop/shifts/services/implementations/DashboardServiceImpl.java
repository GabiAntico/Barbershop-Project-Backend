package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.dashboard.ClientDashboardResponse;
import com.barbershop.shifts.dtos.dashboard.DashboardResponse;
import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.entities.PaymentStatus;
import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.entities.ShiftStatus;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.entities.Visit;
import com.barbershop.shifts.repositories.ClientRepositoryJpa;
import com.barbershop.shifts.repositories.ShiftRepositoryJpa;
import com.barbershop.shifts.repositories.VisitRepositoryJpa;
import com.barbershop.shifts.services.CurrentUserService;
import com.barbershop.shifts.services.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
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

        User owner = currentUserService.getCurrentUser();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<Shift> shifts = shiftRepository.findByDatetimeBetweenAndOwner(start, end, owner);
        List<Visit> visits = visitRepository.findByShiftDatetimeBetweenAndShiftOwner(start, end, owner);

        DashboardResponse response = new DashboardResponse();
        response.setRevenue(buildRevenueStats(visits));
        response.setAttendance(buildAttendanceStats(shifts));

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
        Client client = clientRepository.findByIdAndOwner(clientId, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<Shift> shifts = shiftRepository.findByClientIdAndDatetimeBetweenAndOwner(clientId, start, end, owner);
        List<Visit> selectedMonthVisits = visitRepository.findByShiftClientIdAndShiftDatetimeBetweenAndShiftOwner(clientId, start, end, owner);
        List<Visit> historicalVisits = visitRepository.findByShiftClientIdAndShiftOwner(clientId, owner);

        ClientDashboardResponse response = new ClientDashboardResponse();
        response.setClient(buildClientStats(client));
        response.setAttendance(buildAttendanceStats(shifts));
        response.setSelectedMonthRevenue(buildRevenueStats(selectedMonthVisits));
        response.setHistoricalRevenue(buildRevenueStats(historicalVisits));

        return response;
    }

    private DashboardResponse.RevenueStats buildRevenueStats(List<Visit> visits) {
        List<Visit> paidVisits = visits.stream()
                .filter(visit -> visit.getPaymentStatus() == PaymentStatus.PAID)
                .toList();

        BigDecimal total = paidVisits.stream()
                .map(Visit::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long count = paidVisits.size();
        BigDecimal averageTicket = count == 0
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        DashboardResponse.RevenueStats stats = new DashboardResponse.RevenueStats();
        stats.setTotalPaidAmount(total);
        stats.setPaidVisitsCount(count);
        stats.setAverageTicket(averageTicket);

        return stats;
    }

    private DashboardResponse.AttendanceStats buildAttendanceStats(List<Shift> shifts) {
        LocalDateTime now = LocalDateTime.now();

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

        return stats;
    }
}
