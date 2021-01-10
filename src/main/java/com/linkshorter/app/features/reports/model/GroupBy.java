package com.linkshorter.app.features.reports.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum GroupBy {
    LINK("Link"),
    BROWSER("Przeglądarka"),
    OS("System operacyjny"),
    DEVICE_CLASS("Typ urządzenia"),
    ACTIVITY_DATE("Data aktywności"),
    DAY_HOURS("Podział na godziny dzienne i nocne");
    private String displayValue;


    GroupBy(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public static List<Object> generateValues() {
        return Arrays.asList(GroupBy.LINK, GroupBy.BROWSER, GroupBy.OS, GroupBy.DEVICE_CLASS).stream().map(groupBy -> {
            Map<String, String> newObject = new HashMap<>();
            newObject.put("value", groupBy.name());
            newObject.put("displayValue", groupBy.getDisplayValue());
            return newObject;
        }).collect(Collectors.toList());
    }

    public static GroupBy parse(String value) {
        GroupBy found = null;
        for (GroupBy groupBy : GroupBy.values()) {
            if (groupBy.name().equalsIgnoreCase(value)) {
                found = groupBy;
                break;
            }
        }
        return found;
    }
}
