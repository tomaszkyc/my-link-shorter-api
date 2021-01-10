package com.linkshorter.app.features.reports.converter.strategy;

import com.linkshorter.app.features.links.model.LinkActivity;
import com.linkshorter.app.features.reports.model.GroupBy;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class GroupByConvertStrategy extends ConvertStrategy<List<LinkActivity>, Map<String, Long>> {

    private Object object;

    public GroupByConvertStrategy(Object object) {
        this.object = object;
    }

    @Override
    public Function<List<LinkActivity>, Map<String, Long>> getConvertFunction() {
        GroupBy groupBy = (GroupBy)object;
        return linkActivities -> {
            Map<String, Long> result = null;
            if (groupBy.equals(GroupBy.BROWSER)) {
                result = linkActivities.stream().collect( Collectors.groupingBy(LinkActivity::getAgentName, Collectors.counting()) );
            } else if (groupBy.equals(GroupBy.DEVICE_CLASS)) {
                result = linkActivities.stream().collect( Collectors.groupingBy(LinkActivity::getDeviceClass, Collectors.counting()) );
            } else if (groupBy.equals(GroupBy.OS)) {
                result = linkActivities.stream().collect( Collectors.groupingBy(LinkActivity::getOsName, Collectors.counting()) );
            } else if (groupBy.equals(GroupBy.LINK)) {
                result = linkActivities.stream().collect( Collectors.groupingBy(linkActivity -> linkActivity.getLink().getShortLink(), Collectors.counting()) );
            } else if (groupBy.equals(GroupBy.ACTIVITY_DATE)) {
                result = prepareMapWithActivityDateGroupedData(linkActivities);
            } else if (groupBy.equals(GroupBy.DAY_HOURS)) {
                result = countActivitiesByDayHours(linkActivities);
            }

            return result;
        };
    }


    private Map<String, Long> countActivitiesByDayHours(List<LinkActivity> linkActivities) {
        Function<LinkActivity, String> getHoursPartFromLinkActivityDate = linkActivity -> {
            Date activityDate = linkActivity.getActivityDate();
            return new SimpleDateFormat("HH").format(activityDate);
        };
        return linkActivities.stream().collect( Collectors.groupingBy(getHoursPartFromLinkActivityDate, Collectors.counting()) );
    }

    private Map<String, Long> prepareMapWithActivityDateGroupedData(List<LinkActivity> linkActivities) {
        Map<String, Long> values = new TreeMap<>();
        linkActivities.stream().sorted( Comparator.comparing(LinkActivity::getActivityDate) ).forEach( linkActivity -> {
            Date activityDate = linkActivity.getActivityDate();
            String key = new SimpleDateFormat("yyyy-MM-dd").format(activityDate);
            Long value = values.getOrDefault(key, 0L);
            value++;
            values.put(key, value);
        }  );
        return values;
    }
}
