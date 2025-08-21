package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPriceLookupService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;
    private final TicketPriceLookupService ticketPriceLookupService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService, TicketPriceLookupService ticketPriceLookupService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
        this.ticketPriceLookupService = ticketPriceLookupService;
    }

    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        if(accountId < 1) {
            throw new InvalidPurchaseException("Account Id should be greater than zero");
        }

        if(ticketTypeRequests == null) {
            throw new InvalidPurchaseException("Argument for requests is null");
        }

        if(ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException("No requests for purchase");
        }

        int totalPrice = Arrays.stream(ticketTypeRequests)
                .map(r -> r.getNoOfTickets() * ticketPriceLookupService.priceFor(r.getTicketType()))
                .reduce(0, Integer::sum);

        int totalNoOfTickets = Arrays.stream(ticketTypeRequests)
                .map(TicketTypeRequest::getNoOfTickets)
                .reduce(0, Integer::sum);

        ticketPaymentService.makePayment(accountId, totalPrice);
        seatReservationService.reserveSeat(accountId, totalNoOfTickets);
    }

}
