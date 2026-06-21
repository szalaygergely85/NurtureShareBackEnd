package com.nurtureshare.backend.service;

import com.nurtureshare.backend.dto.request.UpdateSettingsRequest;
import com.nurtureshare.backend.model.enums.BabyGender;
import com.nurtureshare.backend.dto.response.CoupleStatusResponse;
import com.nurtureshare.backend.dto.response.SettingsResponse;
import com.nurtureshare.backend.exception.ResourceNotFoundException;
import com.nurtureshare.backend.model.Couple;
import com.nurtureshare.backend.model.Pregnancy;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.model.UserSettings;
import com.nurtureshare.backend.repository.PregnancyRepository;
import com.nurtureshare.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final CoupleService coupleService;
    private final PregnancyRepository pregnancyRepository;

    public SettingsResponse getSettings(User currentUser) {
        UserSettings settings = userSettingsRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found for user: " + currentUser.getEmail()));

        CoupleStatusResponse coupleStatus = coupleService.getStatus(currentUser);
        PregnancyInfo pregnancy = loadPregnancyInfo(currentUser);

        return toSettingsResponse(settings, currentUser, coupleStatus, pregnancy);
    }

    public SettingsResponse updateSettings(User currentUser, UpdateSettingsRequest req) {
        UserSettings settings = userSettingsRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found for user: " + currentUser.getEmail()));

        if (req.getNotificationsEnabled() != null) {
            settings.setNotificationsEnabled(req.getNotificationsEnabled());
        }
        if (req.getAppMode() != null) {
            settings.setAppMode(req.getAppMode());
            // Keep Pregnancy.appMode in sync so timeline reflects the mode change
            try {
                Couple couple = coupleService.getCoupleForUser(currentUser);
                pregnancyRepository.findByCoupleId(couple.getId()).ifPresent(p -> {
                    p.setAppMode(req.getAppMode());
                    pregnancyRepository.save(p);
                });
            } catch (ResourceNotFoundException ignored) {
            }
        }

        settings = userSettingsRepository.save(settings);
        log.info("Settings updated for user: {}", currentUser.getEmail());

        CoupleStatusResponse coupleStatus = coupleService.getStatus(currentUser);
        PregnancyInfo pregnancy = loadPregnancyInfo(currentUser);

        return toSettingsResponse(settings, currentUser, coupleStatus, pregnancy);
    }

    private PregnancyInfo loadPregnancyInfo(User currentUser) {
        try {
            Couple couple = coupleService.getCoupleForUser(currentUser);
            Optional<Pregnancy> pregnancyOpt = pregnancyRepository.findByCoupleId(couple.getId());
            if (pregnancyOpt.isPresent()) {
                Pregnancy p = pregnancyOpt.get();
                return new PregnancyInfo(p.getDueDate(), p.getCurrentWeek(), p.getTrimester(),
                        p.getBabyGender(), p.getBabyName());
            }
        } catch (ResourceNotFoundException ignored) {
        }
        return new PregnancyInfo(null, null, null, BabyGender.UNKNOWN, null);
    }

    private SettingsResponse toSettingsResponse(UserSettings settings, User user,
                                                 CoupleStatusResponse coupleStatus,
                                                 PregnancyInfo pregnancy) {
        return SettingsResponse.builder()
                .settingsId(settings.getId())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userName(user.getName())
                .notificationsEnabled(settings.isNotificationsEnabled())
                .appMode(settings.getAppMode())
                .coupleStatus(coupleStatus)
                .updatedAt(settings.getUpdatedAt())
                .dueDate(pregnancy.dueDate)
                .currentWeek(pregnancy.currentWeek)
                .trimester(pregnancy.trimester)
                .babyGender(pregnancy.babyGender)
                .babyName(pregnancy.babyName)
                .build();
    }

    private record PregnancyInfo(LocalDate dueDate, Integer currentWeek, String trimester,
                                  BabyGender babyGender, String babyName) {}
}
