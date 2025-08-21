package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPriceLookupService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.CHILD;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.INFANT;

class ValidationContext {
    private final Long accountId;
    private final TicketTypeRequest[] requests;
    private final List<String> errors;

    public ValidationContext(Long accountId, TicketTypeRequest[] requests) {
        this.accountId = accountId;
        this.requests = requests;
        this.errors = new ArrayList<>();
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

    public ValidationContext with(String error) {
        this.errors.add(error);
        return this;
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

    record Totals(int totalNoOfTickets, int totalPrice) {}

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        ValidationContext context = validate(
                this::validateAccountId,
                this::validateRequestsExists,
                this::validateMaxTickets,
                this::validateAdultPresent,
                this::validateOneInfantShouldBeManagedByOneAdult,
                this::validateEnoughAdultsForChildren
        ).apply(new ValidationContext(accountId, ticketTypeRequests));

        if(!context.getErrors().isEmpty()) {
            String msg = String.join("\n", context.getErrors()).trim();
            throw new InvalidPurchaseException(msg);
        }

        Totals totals = Arrays.stream(ticketTypeRequests).collect(
                Collectors.teeing(
                        Collectors.summingInt(TicketTypeRequest::getNoOfTickets),
                        Collectors.summingInt(r -> r.getNoOfTickets() *
                                ticketPriceLookupService.priceFor(r.getTicketType())),
                        Totals::new
                )
        );

        ticketPaymentService.makePayment(accountId, totals.totalPrice);
        seatReservationService.reserveSeat(accountId, totals.totalNoOfTickets);
    }

    @SafeVarargs
    private static <T> Function<T, T> validate(Function<T, T>... fns) {
        return Arrays.stream(fns).reduce(Function.identity(), Function::andThen);
    }

    private ValidationContext validateAccountId(ValidationContext context) {
        if(context.getAccountId() == null || context.getAccountId() < 1) {
            return context.with("Account Id should be greater than zero");
        }
        return context;
    }

    private ValidationContext validateRequestsExists(ValidationContext context) {
        if(context.getRequests() == null || context.getRequests().length == 0) {
            return context.with("Requests array is null or empty");
        }
        return context;
    }

    private ValidationContext validateMaxTickets(ValidationContext context) {
        if(context.getRequests() != null && context.getRequests().length > 25) {
            return context.with("Too many tickets in purchase");
        }
        return context;
    }

    private ValidationContext validateAdultPresent(ValidationContext context) {
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

    private static Collector<TicketTypeRequest, ?, Map<TicketTypeRequest.Type, Integer>> countTicketsByType() {
        return Collectors.toMap(
                TicketTypeRequest::getTicketType,
                TicketTypeRequest::getNoOfTickets,
                Integer::sum,
                () -> new EnumMap<>(TicketTypeRequest.Type.class)
        );
    }

    private ValidationContext validateOneInfantShouldBeManagedByOneAdult(ValidationContext context) {
        if(context.getRequests() != null && context.getRequests().length > 0) {
            Map<TicketTypeRequest.Type, Integer> ticketsPerType =
                    Arrays.stream(context.getRequests()).collect(countTicketsByType());
            if(ticketsPerType.containsKey(ADULT) && ticketsPerType.containsKey(INFANT)) {
                if(ticketsPerType.get(ADULT) < ticketsPerType.get(INFANT)) {
                    return context.with("Not enough adults for infants");
                }
            }
        }
        return context;
    }

    private ValidationContext validateEnoughAdultsForChildren(ValidationContext context) {
        if (context.getRequests() == null || context.getRequests().length == 0) return context;

        Map<TicketTypeRequest.Type, Integer> ticketsPerType =
                Arrays.stream(context.getRequests()).collect(countTicketsByType());

        if(ticketsPerType.containsKey(ADULT) && ticketsPerType.containsKey(CHILD)) {
            int adults   = ticketsPerType.getOrDefault(TicketTypeRequest.Type.ADULT, 0);
            int children = ticketsPerType.getOrDefault(TicketTypeRequest.Type.CHILD, 0);
            int infants  = ticketsPerType.getOrDefault(TicketTypeRequest.Type.INFANT, 0);

            int childrenPerAdult = (infants > 0) ? 3 : 7;

            if (children > 0 && adults * childrenPerAdult < children) {
                return context.with("Not enough adults for children (1 adult per "
                        + childrenPerAdult + " children"
                        + (infants > 0 ? " when infants are present" : "")
                        + ")");
            }
        }

        return context;
    }

}
