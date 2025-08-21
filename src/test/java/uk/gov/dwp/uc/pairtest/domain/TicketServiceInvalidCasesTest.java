package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;

public class TicketServiceInvalidCasesTest {

    long accountId = 1;
    TicketService ticketService;

    TicketPaymentService ticketPaymentService;
    SeatReservationService seatReservationService;
    TicketPriceLookupService ticketPriceLookupService;

    @BeforeEach
    public void setUp() {

        ticketPaymentService = mock(TicketPaymentService.class);
        seatReservationService = mock(SeatReservationService.class);
        ticketPriceLookupService = mock(TicketPriceLookupService.class);


        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService, ticketPriceLookupService);
    }

    @AfterEach
    public void tearDown() {
        reset(ticketPaymentService, seatReservationService, ticketPriceLookupService);
    }

    @Test
    public void test_NullRequests() {

        try {
            ticketService.purchaseTickets(accountId, null);
        } catch (InvalidPurchaseException e) {
            Assertions.assertEquals("Argument for requests is null", e.getMessage());
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }

        fail("should have thrown exception");
    }

    @Test
    public void test_NoRequests() {

        try {
            ticketService.purchaseTickets(accountId, new TicketTypeRequest[]{});
        } catch (InvalidPurchaseException e) {
            Assertions.assertEquals("No requests for purchase", e.getMessage());
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }

        fail("should have thrown exception");
    }

    @Test
    public void test_InvalidAccountId() {

        try {
            ticketService.purchaseTickets(0L, new TicketTypeRequest(ADULT, 1));
        } catch (InvalidPurchaseException e) {
            Assertions.assertEquals("Account Id should be greater than zero", e.getMessage());
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }

        fail("should have thrown exception");
    }

    @Test
    public void test_Over25_Tickets() {

        try {
            TicketTypeRequest[] requests = IntStream.range(0, 26)
                    .mapToObj(i -> new TicketTypeRequest(ADULT, 1))
                    .toArray(TicketTypeRequest[]::new);
            ticketService.purchaseTickets(accountId, requests);
        } catch (InvalidPurchaseException e) {
            Assertions.assertEquals("Too many tickets in purchase", e.getMessage());
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }

        fail("should have thrown exception");
    }
}
