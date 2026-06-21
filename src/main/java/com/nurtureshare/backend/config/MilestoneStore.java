package com.nurtureshare.backend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nurtureshare.backend.dto.response.MilestoneResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class MilestoneStore {

    private final ObjectMapper objectMapper;

    // locale → (week → milestone)
    private final Map<String, TreeMap<Integer, MilestoneResponse>> store = new HashMap<>();

    @PostConstruct
    public void load() {
        loadLocale("milestones.json", "en");
        loadLocale("milestones_hu.json", "hu");
    }

    private void loadLocale(String file, String locale) {
        try {
            List<Map<String, Object>> raw = objectMapper.readValue(
                    new ClassPathResource(file).getInputStream(),
                    new TypeReference<>() {}
            );
            TreeMap<Integer, MilestoneResponse> byWeek = new TreeMap<>();
            for (Map<String, Object> m : raw) {
                int week = (Integer) m.get("week");
                byWeek.put(week, MilestoneResponse.builder()
                        .week(week)
                        .title((String) m.get("title"))
                        .babySize((String) m.get("babySize"))
                        .description((String) m.get("description"))
                        .imageDescription((String) m.get("imageDescription"))
                        .build());
            }
            store.put(locale, byWeek);
            log.info("Loaded {} milestones for locale '{}'", byWeek.size(), locale);
        } catch (Exception e) {
            log.error("Failed to load milestone file '{}': {}", file, e.getMessage());
        }
    }

    public Optional<MilestoneResponse> find(int week, String locale) {
        TreeMap<Integer, MilestoneResponse> byWeek = store.get(locale);
        if (byWeek == null) return Optional.empty();

        // Exact match first, then nearest week at or before
        MilestoneResponse exact = byWeek.get(week);
        if (exact != null) return Optional.of(exact);

        Map.Entry<Integer, MilestoneResponse> floor = byWeek.floorEntry(week);
        return floor != null ? Optional.of(floor.getValue()) : Optional.empty();
    }
}
