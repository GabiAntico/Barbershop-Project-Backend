package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.settings.AppSettingsRequest;
import com.barbershop.shifts.dtos.settings.AppSettingsResponse;
import com.barbershop.shifts.entities.AppSettings;
import com.barbershop.shifts.repositories.AppSettingsRepositoryJpa;
import com.barbershop.shifts.services.AppSettingsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AppSettingsServiceImpl implements AppSettingsService {

    private static final Long SETTINGS_ID = 1L;

    private final AppSettingsRepositoryJpa appSettingsRepository;

    public AppSettingsServiceImpl(AppSettingsRepositoryJpa appSettingsRepository) {
        this.appSettingsRepository = appSettingsRepository;
    }

    @Override
    public AppSettingsResponse getSettings() {
        return convertEntityIntoDto(getOrCreateSettings());
    }

    @Override
    public AppSettingsResponse updateSettings(AppSettingsRequest request) {
        AppSettings settings = getOrCreateSettings();
        settings.setDefaultEstimatedAmount(request.getDefaultEstimatedAmount());

        return convertEntityIntoDto(appSettingsRepository.save(settings));
    }

    private AppSettings getOrCreateSettings() {
        return appSettingsRepository.findById(SETTINGS_ID).orElseGet(() -> {
            AppSettings settings = new AppSettings();
            settings.setId(SETTINGS_ID);
            settings.setDefaultEstimatedAmount(BigDecimal.ZERO);
            return appSettingsRepository.save(settings);
        });
    }

    private AppSettingsResponse convertEntityIntoDto(AppSettings settings) {
        AppSettingsResponse response = new AppSettingsResponse();
        response.setDefaultEstimatedAmount(settings.getDefaultEstimatedAmount());
        return response;
    }
}
