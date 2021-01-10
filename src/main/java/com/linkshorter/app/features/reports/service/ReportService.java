package com.linkshorter.app.features.reports.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.model.LinkActivity;
import com.linkshorter.app.features.links.repository.LinkActivityRepository;
import com.linkshorter.app.features.links.repository.LinkRepository;
import com.linkshorter.app.features.reports.converter.strategy.ConverterStrategyFactory;
import com.linkshorter.app.features.reports.model.GroupBy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class ReportService {

    private final LinkActivityRepository linkActivityRepository;
    private final LinkRepository linkRepository;

    public ReportService(LinkActivityRepository linkActivityRepository, LinkRepository linkRepository) {
        this.linkActivityRepository = linkActivityRepository;
        this.linkRepository = linkRepository;
    }


    public Map<String, Long> generateGroupByReport(GroupBy groupBy, Date dateFrom, Date dateTo, User user) throws Exception {
        List<Link> userLinks = linkRepository.findAllByUser(user);
        List<LinkActivity> linkActivities = linkActivityRepository.findAllByActivityDateIsBetweenAndLinkIsIn(dateFrom, dateTo, userLinks);
        Function<List<LinkActivity>, Map<String, Long>> mappingFunction = ConverterStrategyFactory.createConvertStrategy(groupBy).getConvertFunction();
        return mappingFunction.apply(linkActivities);
    }

}
