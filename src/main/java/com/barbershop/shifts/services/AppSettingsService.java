package com.barbershop.shifts.services;

import com.barbershop.shifts.dtos.settings.AppSettingsRequest;
import com.barbershop.shifts.dtos.settings.AppSettingsResponse;

public interface AppSettingsService {
    AppSettingsResponse getSettings();
    AppSettingsResponse updateSettings(AppSettingsRequest request);
    String getDefaultCurrency();
}
