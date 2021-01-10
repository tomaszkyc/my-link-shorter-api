package com.linkshorter.app.features.reports.converter.strategy;

import com.linkshorter.app.features.links.model.LinkActivity;
import com.linkshorter.app.features.reports.model.GroupBy;

import java.util.List;
import java.util.Map;


public class ConverterStrategyFactory {

    public static ConvertStrategy<List<LinkActivity>, Map<String, Long>> createConvertStrategy(Object object) throws Exception {
        if (object instanceof GroupBy) {
            return new GroupByConvertStrategy(object);
        }
        else {
            throw new IllegalArgumentException("Brak strategi dla obiektu");
        }
    }

}
