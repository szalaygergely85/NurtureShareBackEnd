package com.nurtureshare.backend.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentStore {

    private final ObjectMapper objectMapper;

    private record LocaleContent(
            // week-range → list
            TreeMap<Integer, List<String>> pregnancyActions,
            TreeMap<Integer, List<String>> partnerActions,
            TreeMap<Integer, List<String>> babyActions,
            TreeMap<Integer, List<String>> newbornCareActions,
            TreeMap<Integer, List<String>> postpartumActions,
            TreeMap<Integer, List<String>> babyPartnerActions,
            TreeMap<Integer, List<String>> warningSignsPregnancy,
            TreeMap<Integer, List<String>> warningSignsBaby,
            // week-range → tip string
            TreeMap<Integer, String> partnerTips,
            TreeMap<Integer, String> babyPartnerTips,
            TreeMap<Integer, String> mentalHealthTips,
            // month → list
            TreeMap<Integer, List<String>> monthlyBabyMilestones,
            // flat lists
            List<String> hospitalBagChecklist,
            List<String> newbornEssentialsChecklist,
            List<String> laborSigns
    ) {}

    private final Map<String, LocaleContent> store = new HashMap<>();

    @PostConstruct
    public void load() {
        loadLocale("timeline_content.json", "en");
        loadLocale("timeline_content_hu.json", "hu");
    }

    private void loadLocale(String file, String locale) {
        try {
            JsonNode root = objectMapper.readTree(new ClassPathResource(file).getInputStream());
            store.put(locale, new LocaleContent(
                    parseItemsMap(root.get("pregnancyActions"),       "upToWeek"),
                    parseItemsMap(root.get("partnerActions"),         "upToWeek"),
                    parseItemsMap(root.get("babyActions"),            "upToWeek"),
                    parseItemsMap(root.get("newbornCareActions"),     "upToWeek"),
                    parseItemsMap(root.get("postpartumActions"),      "upToWeek"),
                    parseItemsMap(root.get("babyPartnerActions"),     "upToWeek"),
                    parseItemsMap(root.get("warningSignsPregnancy"),  "upToWeek"),
                    parseItemsMap(root.get("warningSignsBaby"),       "upToWeek"),
                    parseTipMap(root.get("partnerTips")),
                    parseTipMap(root.get("babyPartnerTips")),
                    parseTipMap(root.get("mentalHealthTips")),
                    parseItemsMap(root.get("monthlyBabyMilestones"), "month"),
                    parseStringList(root.get("hospitalBagChecklist")),
                    parseStringList(root.get("newbornEssentialsChecklist")),
                    parseStringList(root.get("laborSigns"))
            ));
            log.info("Loaded timeline content for locale '{}'", locale);
        } catch (Exception e) {
            log.error("Failed to load content file '{}': {}", file, e.getMessage());
        }
    }

    // ── Parsers ───────────────────────────────────────────────────────────────

    private TreeMap<Integer, List<String>> parseItemsMap(JsonNode node, String keyField) {
        TreeMap<Integer, List<String>> map = new TreeMap<>();
        if (node == null || !node.isArray()) return map;
        for (JsonNode entry : node) {
            int key = entry.path(keyField).asInt();
            List<String> items = new ArrayList<>();
            JsonNode itemsNode = entry.get("items");
            if (itemsNode != null && itemsNode.isArray()) {
                itemsNode.forEach(item -> items.add(item.asText()));
            }
            map.put(key, items);
        }
        return map;
    }

    private TreeMap<Integer, String> parseTipMap(JsonNode node) {
        TreeMap<Integer, String> map = new TreeMap<>();
        if (node == null || !node.isArray()) return map;
        for (JsonNode entry : node) {
            map.put(entry.path("upToWeek").asInt(), entry.path("text").asText());
        }
        return map;
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node == null || !node.isArray()) return list;
        node.forEach(item -> list.add(item.asText()));
        return list;
    }

    // ── Public accessors ──────────────────────────────────────────────────────

    public List<String> getPregnancyActions(int week, String locale) {
        return getItems(week, locale, LocaleContent::pregnancyActions);
    }

    public String getPartnerTip(int week, String locale) {
        return getTip(week, locale, LocaleContent::partnerTips);
    }

    public List<String> getPartnerActions(int week, String locale) {
        return getItems(week, locale, LocaleContent::partnerActions);
    }

    /** Fetal development facts shown during pregnancy mode. */
    public List<String> getBabyActions(int week, String locale) {
        return getItems(week, locale, LocaleContent::babyActions);
    }

    /** What the mother should do week-by-week after birth. */
    public List<String> getNewbornCareActions(int week, String locale) {
        return getItems(week, locale, LocaleContent::newbornCareActions);
    }

    public List<String> getPostpartumActions(int week, String locale) {
        return getItems(week, locale, LocaleContent::postpartumActions);
    }

    public String getBabyPartnerTip(int week, String locale) {
        return getTip(week, locale, LocaleContent::babyPartnerTips);
    }

    public List<String> getBabyPartnerActions(int week, String locale) {
        return getItems(week, locale, LocaleContent::babyPartnerActions);
    }

    public List<String> getWarningSignsPregnancy(int week, String locale) {
        return getItems(week, locale, LocaleContent::warningSignsPregnancy);
    }

    public List<String> getWarningSignsBaby(int week, String locale) {
        return getItems(week, locale, LocaleContent::warningSignsBaby);
    }

    public String getMentalHealthTip(int week, String locale) {
        return getTip(week, locale, LocaleContent::mentalHealthTips);
    }

    public List<String> getBabyMilestones(int month, String locale) {
        return getItems(month, locale, LocaleContent::monthlyBabyMilestones);
    }

    public List<String> getHospitalBagChecklist(String locale) {
        return getFlat(locale, LocaleContent::hospitalBagChecklist);
    }

    public List<String> getNewbornEssentialsChecklist(String locale) {
        return getFlat(locale, LocaleContent::newbornEssentialsChecklist);
    }

    public List<String> getLaborSigns(String locale) {
        return getFlat(locale, LocaleContent::laborSigns);
    }

    // ── Generic lookup helpers ────────────────────────────────────────────────

    private List<String> getItems(int key, String locale,
                                   Function<LocaleContent, TreeMap<Integer, List<String>>> selector) {
        List<String> result = lookupCeiling(key, locale, selector);
        if (result != null) return result;
        if (!"en".equals(locale)) result = lookupCeiling(key, "en", selector);
        return result != null ? result : List.of();
    }

    private String getTip(int key, String locale,
                           Function<LocaleContent, TreeMap<Integer, String>> selector) {
        String result = lookupCeiling(key, locale, selector);
        if (result != null) return result;
        if (!"en".equals(locale)) result = lookupCeiling(key, "en", selector);
        return result != null ? result : "";
    }

    private List<String> getFlat(String locale,
                                  Function<LocaleContent, List<String>> selector) {
        LocaleContent content = store.get(locale);
        if (content != null) {
            List<String> result = selector.apply(content);
            if (!result.isEmpty()) return result;
        }
        content = store.get("en");
        return content != null ? selector.apply(content) : List.of();
    }

    private <T> T lookupCeiling(int key, String locale,
                                  Function<LocaleContent, TreeMap<Integer, T>> selector) {
        LocaleContent content = store.get(locale);
        if (content == null) return null;
        var entry = selector.apply(content).ceilingEntry(key);
        return entry != null ? entry.getValue() : null;
    }
}
