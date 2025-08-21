package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.CHILD;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.INFANT;

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
            assertEquals("Requests array is null or empty", e.getMessage());
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
            assertEquals("Requests array is null or empty", e.getMessage());
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
            assertEquals("Account Id should be greater than zero", e.getMessage());
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }

        fail("should have thrown exception");
    }

    @Test
    public void test_NullAccountId() {

        try {
            ticketService.purchaseTickets(null, new TicketTypeRequest(ADULT, 1));
        } catch (InvalidPurchaseException e) {
            assertEquals("Account Id should be greater than zero", e.getMessage());
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
            assertEquals("Too many tickets in purchase", e.getMessage());
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }

        fail("should have thrown exception");
    }

    @Test
    public void test_InfantOnly_Tickets() {

        try {
            ticketService.purchaseTickets(accountId, new TicketTypeRequest(INFANT, 1));
        } catch (InvalidPurchaseException e) {
            assertEquals("Infants or Child only purchase not allowed", e.getMessage());
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }

        fail("should have thrown exception");
    }

    @Test
    public void test_ChildOnly_Tickets() {

        try {
            ticketService.purchaseTickets(accountId, new TicketTypeRequest(CHILD, 1));
        } catch (InvalidPurchaseException e) {
            assertEquals("Infants or Child only purchase not allowed", e.getMessage());
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }

        fail("should have thrown exception");
    }

    @Test
    public void test_ChildAndInfantOnly_Tickets() {

        try {
            ticketService.purchaseTickets(accountId,
                    new TicketTypeRequest(CHILD, 1),
                    new TicketTypeRequest(INFANT, 1)
            );
        } catch (InvalidPurchaseException e) {
            assertEquals("Infants or Child only purchase not allowed", e.getMessage());
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }

        fail("should have thrown exception");
    }

    @Test
    public void test_ChildAndInfantOnly_And_InvalidAccountId_Tickets() {

        try {
            ticketService.purchaseTickets(0L,
                    new TicketTypeRequest(CHILD, 1),
                    new TicketTypeRequest(INFANT, 1)
            );
        } catch (InvalidPurchaseException e) {
            assertEquals(true, e.getMessage().contains("Infants or Child only purchase not allowed"));
            assertEquals(true, e.getMessage().contains("Account Id should be greater than zero"));
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }

        fail("should have thrown exception");
    }

    @Test
    public void test_ThereCantBe_MoreInfantsThanAdults() {
        try {
            ticketService.purchaseTickets(1L,
                    new TicketTypeRequest(INFANT, 2),
                    new TicketTypeRequest(ADULT, 1)
            );
        } catch (InvalidPurchaseException e) {
            assertEquals(true, e.getMessage().contains("Not enough adults for infants"));
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }
        fail("should have thrown exception");
    }

    @Test
    public void test_ForEvery7_Children_ThereShouldBe_NoLessThan_OneAdult() {
        try {
            ticketService.purchaseTickets(1L,
                    new TicketTypeRequest(CHILD, 8),
                    new TicketTypeRequest(ADULT, 1)
            );
        } catch (InvalidPurchaseException e) {
            assertEquals(true, e.getMessage().contains("Not enough adults for children"));
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }
        fail("should have thrown exception");
    }

    @Test
    public void test_ForEvery3_Children_ThereShouldBe_NoLessThan_OneAdult_WhenThereAreInfants() {
        try {
            ticketService.purchaseTickets(1L,
                    new TicketTypeRequest(CHILD, 6),
                    new TicketTypeRequest(INFANT, 1),
                    new TicketTypeRequest(ADULT, 1)
            );
        } catch (InvalidPurchaseException e) {
            assertEquals(true, e.getMessage().contains("Not enough adults for children"));
            verify(ticketPaymentService, times(0)).makePayment(accountId, 0);
            verify(seatReservationService, times(0)).reserveSeat(accountId, 0);
            return;
        }
        fail("should have thrown exception");
    }

}
