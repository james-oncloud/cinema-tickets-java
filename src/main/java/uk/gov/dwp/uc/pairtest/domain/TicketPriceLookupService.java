package uk.gov.dwp.uc.pairtest.domain;

public interface TicketPriceLookupService {
    int priceFor(TicketTypeRequest.Type type);
}
