package com.nurtureshare.backend.service;

import com.nurtureshare.backend.dto.request.PregnancySetupRequest;
import com.nurtureshare.backend.dto.request.UpdateBabyInfoRequest;
import com.nurtureshare.backend.model.enums.AppMode;
import com.nurtureshare.backend.model.enums.BabyGender;
import com.nurtureshare.backend.model.enums.UserRole;
import com.nurtureshare.backend.config.ContentStore;
import com.nurtureshare.backend.config.MilestoneStore;
import com.nurtureshare.backend.dto.response.MilestoneResponse;
import com.nurtureshare.backend.dto.response.TimelineResponse;
import com.nurtureshare.backend.exception.ResourceNotFoundException;
import com.nurtureshare.backend.model.Couple;
import com.nurtureshare.backend.model.Pregnancy;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.repository.PregnancyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TimelineService {

    private final CoupleService coupleService;
    private final PregnancyRepository pregnancyRepository;
    private final MilestoneStore milestoneStore;
    private final ContentStore contentStore;

    public TimelineResponse getTimeline(User currentUser, String locale) {
        Couple couple = coupleService.getCoupleForUser(currentUser);

        Pregnancy pregnancy = pregnancyRepository.findByCoupleId(couple.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No pregnancy record found for this couple."));

        TimelineResponse response = buildTimelineResponse(pregnancy, resolveLocale(locale), currentUser.getRole());
        response.setPartnerConnected(couple.isConnected());
        return response;
    }

    private String resolveLocale(String acceptLanguage) {
        if (acceptLanguage != null && acceptLanguage.toLowerCase().startsWith("hu")) return "hu";
        return "en";
    }

    @Transactional
    public TimelineResponse setupPregnancy(User currentUser, PregnancySetupRequest req, String locale) {
        Couple couple = coupleService.getCoupleForUser(currentUser);

        Pregnancy pregnancy = pregnancyRepository.findByCoupleId(couple.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No pregnancy record found for this couple."));

        pregnancy.setDueDate(req.getDueDate());
        if (req.getAppMode() != null) {
            pregnancy.setAppMode(req.getAppMode());
        }
        pregnancy = pregnancyRepository.save(pregnancy);
        log.info("Updated due date for couple {} to {}", couple.getId(), req.getDueDate());

        TimelineResponse response = buildTimelineResponse(pregnancy, resolveLocale(locale), currentUser.getRole());
        response.setPartnerConnected(couple.isConnected());
        return response;
    }

    @Transactional
    public TimelineResponse updateBabyInfo(User currentUser, UpdateBabyInfoRequest req) {
        Couple couple = coupleService.getCoupleForUser(currentUser);

        Pregnancy pregnancy = pregnancyRepository.findByCoupleId(couple.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No pregnancy record found for this couple."));

        if (req.getBabyGender() != null) {
            pregnancy.setBabyGender(req.getBabyGender());
        }
        if (req.getBabyName() != null) {
            pregnancy.setBabyName(req.getBabyName().isBlank() ? null : req.getBabyName().trim());
        }

        pregnancy = pregnancyRepository.save(pregnancy);
        log.info("Updated baby info for couple {}", couple.getId());

        TimelineResponse response = buildTimelineResponse(pregnancy, "en", currentUser.getRole());
        response.setPartnerConnected(couple.isConnected());
        return response;
    }

    private TimelineResponse buildTimelineResponse(Pregnancy pregnancy, String locale, UserRole role) {
        if (pregnancy.getAppMode() == AppMode.NEWBORN) {
            return buildBabyModeResponse(pregnancy, role, locale);
        } else {
            return buildPregnancyModeResponse(pregnancy, locale, role);
        }
    }

    private String resolveName(Pregnancy pregnancy) {
        return (pregnancy.getBabyName() != null && !pregnancy.getBabyName().isBlank())
                ? pregnancy.getBabyName() : "Baby";
    }

    private String applyName(String template, String name) {
        return template == null ? null : template.replace("{name}", name);
    }

    private String applyGender(String template, BabyGender gender) {
        if (template == null) return null;
        boolean boy = gender == BabyGender.BOY;
        boolean girl = gender == BabyGender.GIRL;
        return template
                .replace("{he}", boy ? "he" : girl ? "she" : "they")
                .replace("{He}", boy ? "He" : girl ? "She" : "They")
                .replace("{him}", boy ? "him" : girl ? "her" : "them")
                .replace("{his}", boy ? "his" : girl ? "her" : "their");
    }

    private String applyAll(String template, String name, BabyGender gender) {
        return applyGender(applyName(template, name), gender);
    }

    private String applyWeek(String template, int week) {
        return template == null ? null : template.replace("{week}", String.valueOf(week));
    }

    private String applyAllWithWeek(String template, String name, BabyGender gender, int week) {
        return applyWeek(applyAll(template, name, gender), week);
    }

    private TimelineResponse buildPregnancyModeResponse(Pregnancy pregnancy, String locale, UserRole role) {
        int week = pregnancy.getCurrentWeek();
        String name = resolveName(pregnancy);
        boolean isPartner = role == UserRole.PARTNER;

        Optional<MilestoneResponse> milestoneOpt = milestoneStore.find(week, locale);
        // fall back to English if locale has no data
        if (milestoneOpt.isEmpty() && !locale.equals("en")) {
            milestoneOpt = milestoneStore.find(week, "en");
        }

        MilestoneResponse milestoneResponse = milestoneOpt.map(m -> MilestoneResponse.builder()
                .week(m.getWeek()).title(m.getTitle())
                .babySize(m.getBabySize())
                .description(applyName(m.getDescription(), name))
                .imageDescription(m.getImageDescription()).build()
        ).orElse(null);

        List<String> actions = isPartner
                ? contentStore.getPartnerActions(week, locale).stream()
                        .map(a -> applyAllWithWeek(a, name, BabyGender.UNKNOWN, week)).toList()
                : contentStore.getPregnancyActions(week, locale).stream()
                        .map(a -> applyWeek(a, week)).toList();

        String checklistTitle = null;
        List<String> checklistItems = null;
        if (week >= 36) {
            checklistTitle = "Signs of Labor";
            checklistItems = contentStore.getLaborSigns(locale);
        } else if (week >= 28) {
            checklistTitle = "Hospital Bag";
            checklistItems = contentStore.getHospitalBagChecklist(locale);
        } else if (week >= 20) {
            checklistTitle = "Newborn Essentials";
            checklistItems = contentStore.getNewbornEssentialsChecklist(locale);
        }

        return TimelineResponse.builder()
                .currentWeek(week)
                .trimester(pregnancy.getTrimester())
                .percentComplete(pregnancy.getPercentComplete())
                .weeksLeft(pregnancy.getWeeksLeft())
                .dueDate(pregnancy.getDueDate())
                .appMode(pregnancy.getAppMode())
                .currentMilestone(milestoneResponse)
                .suggestedActions(actions)
                .partnerFocusTip(applyAllWithWeek(contentStore.getPartnerTip(week, locale), name, BabyGender.UNKNOWN, week))
                .checklistTitle(checklistTitle)
                .checklistItems(checklistItems)
                .build();
    }

    private TimelineResponse buildBabyModeResponse(Pregnancy pregnancy, UserRole role, String locale) {
        int babyWeeks = pregnancy.getBabyAgeWeeks();
        int percentOfFirstYear = (int) Math.min(100, (babyWeeks / 52.0) * 100);
        String name = resolveName(pregnancy);
        BabyGender gender = pregnancy.getBabyGender() != null ? pregnancy.getBabyGender() : BabyGender.UNKNOWN;
        boolean isPartner = role == UserRole.PARTNER;

        List<String> rawActions = isPartner
                ? contentStore.getBabyPartnerActions(babyWeeks, locale)
                : contentStore.getNewbornCareActions(babyWeeks, locale);
        List<String> actions = rawActions.stream().map(a -> applyAll(a, name, gender)).toList();

        String partnerTip = applyAll(contentStore.getBabyPartnerTip(babyWeeks, locale), name, gender);

        return TimelineResponse.builder()
                .currentWeek(babyWeeks)
                .trimester(pregnancy.getBabyStage())
                .percentComplete(percentOfFirstYear)
                .weeksLeft(Math.max(0, 52 - babyWeeks))
                .dueDate(pregnancy.getDueDate())
                .appMode(pregnancy.getAppMode())
                .currentMilestone(null)
                .suggestedActions(actions)
                .partnerFocusTip(partnerTip)
                .babyName(pregnancy.getBabyName())
                .babyGender(gender.name())
                .build();
    }

}
