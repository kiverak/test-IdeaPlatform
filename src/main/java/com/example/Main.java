package com.example;

import com.example.enums.TicketSourceType;
import com.example.model.Ticket;
import com.example.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        final Logger log = LoggerFactory.getLogger(Main.class);
        TicketService ticketService = new TicketService();

        List<Ticket> tickets = ticketService.readTickets("ticket/tickets.json", TicketSourceType.FILE);
        log.info("Successfully parsed " + tickets.size() + " tickets.");

        String origin = "VVO";
        String destination = "TLV";
        Map<String, Duration> MinimalTimeForEachCarrierMap = ticketService.getMinimalTimeForEachCarrier(tickets, origin, destination);
        for (Map.Entry<String, Duration> entry : MinimalTimeForEachCarrierMap.entrySet()) {
            Duration duration = entry.getValue();
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            System.out.printf("Carrier: %s, minimal flight time: %d hours %d min%n", entry.getKey(), hours, minutes);
        }

        BigDecimal averagePrice = ticketService.getAveragePrice(tickets);
        System.out.printf("Average price: %s%n", averagePrice);
        BigDecimal medianaPrice = ticketService.getMedianaPrice(tickets);
        System.out.printf("Mediana price: %s%n", medianaPrice);
        System.out.printf("Average price - mediana price: %s%n", averagePrice.subtract(medianaPrice));
    }
}