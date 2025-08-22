package uk.gov.dwp.uc.pairtest.domain;

public interface CinemaTicketsConfigService {

    String KEY_MAX_PURCHASE_TICKETS = "MAX_PURCHASE_TICKETS";
    String KEY_MAX_CHILDREN_WITH_ADULT = "MAX_CHILDREN_WITH_ADULT";
    String KEY_MAX_CHILDREN_WITH_ADULT_WITH_INFANT = "MAX_CHILDREN_WITH_ADULT_WITH_INFANT";

    int getIntConfig(String key);
}