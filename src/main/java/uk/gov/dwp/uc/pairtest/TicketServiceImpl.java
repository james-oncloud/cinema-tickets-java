package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPriceLookupService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;

class PurchaseTicketsContext {
    private final Long accountId;
    private final TicketTypeRequest[] requests;
    private final List<String> errors;

    public PurchaseTicketsContext(Long accountId, TicketTypeRequest[] requests) {
        this.accountId = accountId;
        this.requests = requests;
        this.errors = new ArrayList<>();
    }

    public PurchaseTicketsContext(Long accountId, TicketTypeRequest[] requests, List<String> errors) {
        this.accountId = accountId;
        this.requests = requests;
        this.errors = errors;
    }

    public Long getAccountId() {
        return accountId;
    }

    public TicketTypeRequest[] getRequests() {
        return requests;
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public PurchaseTicketsContext with(String error) {
        this.errors.add(error);
        return new PurchaseTicketsContext(this.accountId, this.requests, this.errors);
    }
}

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

        PurchaseTicketsContext context = chain(
                this::validateAccountId,
                this::validateRequestsExists,
                this::validateMaxTickets,
                this::validateAdultPresent
        ).apply(new PurchaseTicketsContext(accountId, ticketTypeRequests));

        if(!context.getErrors().isEmpty()) {
            String msg = String.join("\n", context.getErrors());
            throw new InvalidPurchaseException(msg.trim());
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

    @SafeVarargs
    public static <T> Function<T, T> chain(Function<T, T>... fns) {
        return Arrays.stream(fns).reduce(Function.identity(), Function::andThen);
    }

    private PurchaseTicketsContext validateAccountId(PurchaseTicketsContext context) {
        if(context.getAccountId() == null || context.getAccountId() < 1) {
            return context.with("Account Id should be greater than zero");
        }
        return context;
    }

    private PurchaseTicketsContext validateRequestsExists(PurchaseTicketsContext context) {
        if(context.getRequests() == null || context.getRequests().length == 0) {
            return context.with("Requests array is null or empty");
        }
        return context;
    }

    private PurchaseTicketsContext validateMaxTickets(PurchaseTicketsContext context) {
        if(context.getRequests() != null && context.getRequests().length > 25) {
            return context.with("Too many tickets in purchase");
        }
        return context;
    }

    private PurchaseTicketsContext validateAdultPresent(PurchaseTicketsContext context) {
        if(context.getRequests() != null && context.getRequests().length > 0) {
            boolean includeAdult = Arrays.stream(context.getRequests())
                    .collect(Collectors.groupingBy(TicketTypeRequest::getTicketType))
                    .containsKey(ADULT);
            boolean noAdultsIncluded = !includeAdult;
            if(noAdultsIncluded) {
                return context.with("Infants or Child only purchase not allowed");
            }
        }
        return context;
    }

}
