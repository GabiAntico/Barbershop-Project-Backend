package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.settings.AppSettingsRequest;
import com.barbershop.shifts.dtos.settings.AppSettingsResponse;
import com.barbershop.shifts.entities.AppSettings;
import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.repositories.AppSettingsRepositoryJpa;
import com.barbershop.shifts.services.AppSettingsService;
import com.barbershop.shifts.services.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class AppSettingsServiceImpl implements AppSettingsService {

    public static final String FALLBACK_DEFAULT_SLOTS = "10:00,10:30,11:00,11:30,12:00,12:30,13:00,13:30,14:00,14:30,15:00,15:30,16:00,16:30,17:00,17:30,18:00,18:30,19:00,19:30,20:00";
    public static final String FALLBACK_DEFAULT_CURRENCY = "ARS";
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("ARS", "USD", "BRL", "UYU", "CLP");

    private final AppSettingsRepositoryJpa appSettingsRepository;
    private final CurrentUserService currentUserService;

    public AppSettingsServiceImpl(AppSettingsRepositoryJpa appSettingsRepository, CurrentUserService currentUserService) {
        this.appSettingsRepository = appSettingsRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public AppSettingsResponse getSettings() {
        return convertEntityIntoDto(getOrCreateSettings());
    }

    @Override
    public AppSettingsResponse updateSettings(AppSettingsRequest request) {
        AppSettings settings = getOrCreateSettings();
        settings.setDefaultEstimatedAmount(request.getDefaultEstimatedAmount());
        settings.setDefaultCurrency(normalizeCurrency(request.getDefaultCurrency()));

        return convertEntityIntoDto(appSettingsRepository.save(settings));
    }

    @Override
    public String getDefaultCurrency() {
        return getOrCreateSettings().getDefaultCurrency();
    }

    private AppSettings getOrCreateSettings() {
        User owner = currentUserService.getCurrentUser();
        Branch branch = currentUserService.getCurrentBranch();

        AppSettings settings = appSettingsRepository.findFirstByBranchOrderByIdAsc(branch)
                .or(() -> appSettingsRepository.findFirstByOwnerOrderByIdAsc(owner).map(existing -> {
                    existing.setBranch(branch);
                    return appSettingsRepository.save(existing);
                }))
                .orElseGet(() -> {
            AppSettings created = new AppSettings();
            created.setDefaultEstimatedAmount(BigDecimal.ZERO);
            created.setDefaultCurrency(FALLBACK_DEFAULT_CURRENCY);
            created.setDefaultScheduleSlots(FALLBACK_DEFAULT_SLOTS);
            created.setOwner(owner);
            created.setBranch(branch);
            return appSettingsRepository.save(created);
        });

        if (settings.getDefaultScheduleSlots() == null || settings.getDefaultScheduleSlots().trim().isEmpty()) {
            settings.setDefaultScheduleSlots(FALLBACK_DEFAULT_SLOTS);
            return appSettingsRepository.save(settings);
        }

        if (settings.getDefaultCurrency() == null || settings.getDefaultCurrency().trim().isEmpty()) {
            settings.setDefaultCurrency(FALLBACK_DEFAULT_CURRENCY);
            return appSettingsRepository.save(settings);
        }

        return settings;
    }

    private AppSettingsResponse convertEntityIntoDto(AppSettings settings) {
        AppSettingsResponse response = new AppSettingsResponse();
        response.setDefaultEstimatedAmount(settings.getDefaultEstimatedAmount());
        response.setDefaultCurrency(settings.getDefaultCurrency());
        return response;
    }

    private String normalizeCurrency(String value) {
        String currency = value == null || value.trim().isEmpty()
                ? FALLBACK_DEFAULT_CURRENCY
                : value.trim().toUpperCase();

        if (!SUPPORTED_CURRENCIES.contains(currency)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency");
        }

        return currency;
    }
}
