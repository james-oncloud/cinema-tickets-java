package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

public class TicketServiceTest {

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
}
