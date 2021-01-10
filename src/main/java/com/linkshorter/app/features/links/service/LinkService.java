package com.linkshorter.app.features.links.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.model.UserAuthority;
import com.linkshorter.app.features.exception.UserExceedLinkLimitException;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.model.LinkActivity;
import com.linkshorter.app.features.links.repository.LinkActivityRepository;
import com.linkshorter.app.features.links.repository.LinkRepository;
import com.linkshorter.app.features.links.setter.LinkSetter;
import com.linkshorter.app.features.links.validator.LinkValidator;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.ToLongFunction;

@Service
public class LinkService {

    private static final Logger log = LoggerFactory.getLogger(LinkService.class);
    private static final int MAX_NUMBER_OF_CHECKS_IN_REPOSITORY = 50;
    private final LinkRepository linkRepository;
    private final LinkActivityRepository linkActivityRepository;
    private final LinkValidator linkValidator;
    private final LinkSetter linkSetter;

    public LinkService(LinkRepository linkRepository, LinkValidator linkValidator,
                       LinkSetter linkSetter, LinkActivityRepository linkActivityRepository) {
        this.linkRepository = linkRepository;
        this.linkValidator = linkValidator;
        this.linkSetter = linkSetter;
        this.linkActivityRepository = linkActivityRepository;
    }

    public Link createLink(Link link) throws Exception {
        String shortLink = link.getShortLink();
        boolean linkExists = linkRepository.existsByShortLink(shortLink);
        if (linkExists) {
            throw new Exception("Ten krótki link: " + shortLink + " jest już użyty. " +
                    "Wprowadź inny i spróbuj ponownie.");
        }
        linkSetter.set(link);
        linkValidator.validate(link);
        validateIfUserNotExceedLinkLimit(link);
        return linkRepository.save(link);
    }

    public String createUniqueShortLink() {
        boolean foundUnique = false;
        String generatedUniqueShortLink = "";
        int startingNumberOfChars = 6;
        while(!foundUnique) {
            int checkInRepositoryCounter = 0;
            while(checkInRepositoryCounter <= MAX_NUMBER_OF_CHECKS_IN_REPOSITORY) {
                generatedUniqueShortLink = RandomStringUtils.randomAlphanumeric(startingNumberOfChars);
                foundUnique = !linkRepository.existsByShortLink(generatedUniqueShortLink);
                checkInRepositoryCounter++;
                if (foundUnique) {
                    return generatedUniqueShortLink;
                }
            }
            startingNumberOfChars++;
        }
        return generatedUniqueShortLink;
    }

    @Transactional
    public void deleteLink(Link link, User user) throws Exception {
        UUID linkId = link.getId();
        boolean hasAccessToLink = this.hasUserAccessToLink(linkId, user);
        if (!hasAccessToLink) {
            throw new Exception("Nie masz uprawnień do pobrania szczegółów linku o id: " + linkId);
        }
        List<LinkActivity> linkActivities = linkActivityRepository.findLinkActivitiesByLink(link);
        linkActivityRepository.deleteAll(linkActivities);
        linkRepository.deleteById(linkId);
    }

    public List<Link> findAllByUser(User user) {
        return linkRepository.findAllByUser(user);
    }

    public Optional<Link> findByIdAndUser(UUID id, User user) {
        return linkRepository.findByIdAndUser(id, user);
    }

    public Optional<Link> findByShortLinkAndActiveIsTrueAAndExpirationDateBeforeNow(String shortLink) {
        return linkRepository.findByShortLinkAndActiveIsTrueAndExpirationDateAfter(shortLink, new Date());
    }

    public Link updateLink(UUID id, Link linkWithNewData, User user) throws Exception {
        Optional<Link> optionalLink = this.findByIdAndUser(id, user);
        if (optionalLink.isEmpty()) {
            throw new Exception("Nie masz uprawnień do pobrania szczegółów linku o id: " + id);
        }
        Link link = optionalLink.get();
        link.setExpirationDate(linkWithNewData.getExpirationDate());
        link.setActive(linkWithNewData.isActive());
        link.setLongLink(linkWithNewData.getLongLink());
        link.setShortLink( linkWithNewData.getShortLink() );
        linkValidator.validate(link);
        return linkRepository.save(link);
    }

    public boolean hasUserAccessToLink(UUID linkId, User user) {
        Objects.requireNonNull(user, "Can't check access to link for nullable user");
        Objects.requireNonNull(linkId, "Can't check access to link for nullable link id");
        return linkRepository.existsLinkByIdAndUser(linkId, user);
    }

    private void validateIfUserNotExceedLinkLimit(Link link) throws UserExceedLinkLimitException {
        User user = link.getUser();
        if (user != null) {
            long numberOfUserLinks = linkRepository.countLinksByUser(user);
            long userLimit = this.getUserLinkLimit(user);
            if (numberOfUserLinks + 1 > userLimit) {
                throw new UserExceedLinkLimitException(user, userLimit);
            }
        }
    }

    private long getUserLinkLimit(User user) {
        Objects.requireNonNull(user, "Given user object is null or empty");
        long limit = 0;
        if (user.getUserAuthorities() != null && user.getUserAuthorities().size() > 0) {
            ToLongFunction<UserAuthority> getLimitForUserAuthority = userAuthority -> {
                long userAuthorityLimit = 0;
                String userAuthorityName = userAuthority.getAuthority();
                switch (userAuthorityName) {
                    case "admin":
                        userAuthorityLimit = Long.MAX_VALUE;
                        break;
                    case "registered-user":
                        userAuthorityLimit = 20;
                        break;
                    case "premium-user":
                        userAuthorityLimit = 2000;
                        break;
                }
                return userAuthorityLimit;
            };
            for(UserAuthority userAuthority : user.getUserAuthorities()) {
                long limitForUserAuthority = getLimitForUserAuthority.applyAsLong(userAuthority);
                if (limit < limitForUserAuthority) {
                    limit = limitForUserAuthority;
                }
            }
        }
        return limit;
    }
}
