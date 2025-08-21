# Cinema Tickets Java

Senior Java Engineer

Application ID number: 14156847

Thank you for your application for the role of Senior Java Engineer. We have looked at your application and would like to progress your application to the next stage of the recruitment process, which is a coding exercise. A template project can be found here https://github.com/dwp/cinema-tickets

Below you will find instructions with constraints for Java. These will give you details of the exercise and all the acceptance criteria needed to build your solution. You have 5 working days to complete the task, we will not accept late submissions unless agreed, you will be withdrawn from the campaign on working day 6.

1.    When returning the completed test, could you please complete it using Java 11 or later.
2.    Publish your submission to GitHub and make sure it is public.
3.    Send a link to the Github repo to the following email: Digital.EngineeringRecruitment@dwp.gov.uk within 5 working days of receipt of this email.
4.    Once you’ve submitted your work, do not make any further edits.

Please quote your candidate number and campaign number in the subject header, otherwise there may be delays in processing your submission and or risk of not being assessed.
By submitting your solution to us you assert that it is wholly your original work and not a copy. There is no stated time limit but allow at least few hours. The important thing is to arrive at a solution that you are satisfied with and comfortable discussing with others.

Instructions:
# Objective
This is a coding exercise which will allow you to demonstrate how you code and your approach to a given problem.

You will be assessed on:
- Your ability to write clean, well-tested and reusable code.
- How you have ensured the following business rules are correctly met.

# Business Rules
- There are 3 types of tickets i.e. Infant, Child, and Adult.
- The ticket prices are based on the type of ticket (see table below).
- The ticket purchaser declares how many and what type of tickets they want to buy.
- Multiple tickets can be purchased at any given time.
- Only a maximum of 25 tickets that can be purchased at a time.
- Infants do not pay for a ticket and are not allocated a seat. They will be sitting on an Adult's lap.
- Child and Infant tickets cannot be purchased without purchasing an Adult ticket.

|   Ticket Type    |     Price   |
| ---------------- | ----------- |
|    INFANT        |    £0       |
|    CHILD         |    £15     |
|    ADULT         |    £25      |
- There is an existing `TicketPaymentService` responsible for taking payments.
- There is an existing `SeatReservationService` responsible for reserving seats.
## Constraints

- The TicketService interface CANNOT be modified.
- The code in the thirdparty.* packages CANNOT be modified.
- The `TicketTypeRequest` SHOULD be an immutable object.
## Assumptions

You can assume:
- All accounts with an id greater than zero are valid. They also have sufficient funds to pay for any no of tickets.
- The `TicketPaymentService` implementation is an external provider with no defects. You do not need to worry about how the actual payment happens.
- The payment will always go through once a payment request has been made to the `TicketPaymentService`.
- The `SeatReservationService` implementation is an external provider with no defects. You do not need to worry about how the seat reservation algorithm works.
- The seat will always be reserved once a reservation request has been made to the `SeatReservationService`.
## Your Task

Provide a working implementation of a `TicketService` that:
- Considers the above objective, business rules, constraints & assumptions.
- Calculates the correct amount for the requested tickets and makes a payment request to the `TicketPaymentService`.
- Calculates the correct no of seats to reserve and makes a seat reservation request to the `SeatReservationService`.
- Rejects any invalid ticket purchase requests. It is up to you to identify what should be deemed as an invalid purchase request.”


Kind regards

Department for Work and Pensions recruitment team
