package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.CHILD;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.INFANT;

public class TicketServiceValidCasesTest {

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
    public void test_Purchase_OneAdultTicket() {

        when(ticketPriceLookupService.priceFor(ADULT)).thenReturn(25);

        ticketService.purchaseTickets(accountId, new TicketTypeRequest(ADULT, 1));

        verify(ticketPaymentService, times(1)).makePayment(accountId, 25);
        verify(seatReservationService, times(1)).reserveSeat(accountId, 1);
    }

    @Test
    public void test_Purchase_TwoAdultTickets() {

        when(ticketPriceLookupService.priceFor(ADULT)).thenReturn(25);

        ticketService.purchaseTickets(accountId, new TicketTypeRequest(ADULT, 2));

        verify(ticketPaymentService, times(1)).makePayment(accountId, 50);
        verify(seatReservationService, times(1)).reserveSeat(accountId, 2);
    }

    @Test
    public void test_Purchase_OneAdultAndOneChild_Tickets() {

        when(ticketPriceLookupService.priceFor(ADULT)).thenReturn(25);
        when(ticketPriceLookupService.priceFor(CHILD)).thenReturn(15);

        ticketService.purchaseTickets(accountId, new TicketTypeRequest(ADULT, 1), new TicketTypeRequest(CHILD, 1));

        verify(ticketPaymentService, times(1)).makePayment(accountId, 40);
        verify(seatReservationService, times(1)).reserveSeat(accountId, 2);
    }

    @Test
    public void test_Purchase_OneAdultAndOneChildOneInfant_Tickets() {

        when(ticketPriceLookupService.priceFor(ADULT)).thenReturn(25);
        when(ticketPriceLookupService.priceFor(CHILD)).thenReturn(15);
        when(ticketPriceLookupService.priceFor(INFANT)).thenReturn(0);

        ticketService.purchaseTickets(accountId,
                new TicketTypeRequest(ADULT, 1),
                new TicketTypeRequest(CHILD, 1),
                new TicketTypeRequest(INFANT, 0)
        );

        verify(ticketPaymentService, times(1)).makePayment(accountId, 40);
        verify(seatReservationService, times(1)).reserveSeat(accountId, 2);
    }
}
