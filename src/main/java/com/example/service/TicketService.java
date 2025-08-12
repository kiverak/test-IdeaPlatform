package com.example.service;

import com.example.enums.TicketSourceType;
import com.example.mapper.TicketsMapper;
import com.example.model.Ticket;
import com.example.model.TicketsWrapper;
import com.example.utils.TimeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<Ticket> readTickets(String jsonOrPath, TicketSourceType sourceType) {
        if (sourceType == null) {
            log.error("Source type cannot be null");
            throw new IllegalArgumentException("Source type cannot be null");
        }
        switch (sourceType) {
            case API:
                try {
                    return readTickets(jsonOrPath);
                } catch (IllegalArgumentException e) {
                    log.error("Cannot read source json");
                    throw new RuntimeException(e);
                } catch (JsonProcessingException e) {
                    log.error("JSON parsing error");
                    throw new RuntimeException(e);
                }
            case FILE:
                try {
                    return TicketsMapper.readTickets(jsonOrPath);
                } catch (IOException e) {
                    log.error("Cannot read source file");
                    throw new RuntimeException(e);
                }
            default: {
                log.error("Unknown source type: {}", sourceType);
                throw new IllegalArgumentException("Unknown source type");
            }
        }
    }

    public List<Ticket> readTickets(String json) throws IllegalArgumentException, JsonProcessingException {
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("Json reading error");
        }

        TicketsWrapper wrapper = objectMapper.readValue(json, TicketsWrapper.class);

        return wrapper.tickets() != null ? wrapper.tickets() : Collections.emptyList();
    }

    public Map<String, Duration> getMinimalTimeForEachCarrier(
            List<Ticket> tickets, String origin, String destination) {
        Map<String, List<Ticket>> carrierFlightsMap = new HashMap<>();
        for (Ticket ticket : tickets) {
            if (!ticket.origin().equals(origin) || !ticket.destination().equals(destination)) continue;
            carrierFlightsMap.computeIfAbsent(ticket.carrier(), k -> new ArrayList<>()).add(ticket);
        }

        Map<String, Duration> minFlightTimes = new HashMap<>();
        for (Map.Entry<String, List<Ticket>> entry : carrierFlightsMap.entrySet()) {
            String carrier = entry.getKey();
            Duration minFlightTime = getMinFlightTime(entry.getValue());
            minFlightTimes.put(carrier, minFlightTime);
        }

        return minFlightTimes;
    }

    private Duration getMinFlightTime(List<Ticket> tickets) {
        if (tickets.isEmpty()) throw new IllegalArgumentException("Tickets list is empty");
        Duration minDiration = null;
        for (Ticket ticket : tickets) {
            Duration duration = getFlightDuration(ticket);
            if (minDiration == null || duration.compareTo(minDiration) < 0) {
                minDiration = duration;
            }
        }

        return minDiration;
    }

    private Duration getFlightDuration(Ticket ticket) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

        LocalDateTime departure = LocalDateTime.of(
                LocalDate.parse(ticket.departureDate(), dateFormatter),
                LocalTime.parse(ticket.departureTime(), timeFormatter));

        LocalDateTime arrival = LocalDateTime.of(
                LocalDate.parse(ticket.arrivalDate(), dateFormatter),
                LocalTime.parse(ticket.arrivalTime(), timeFormatter));

        Duration timeZoneCorrection = TimeUtils.getTimeZoneCorrection(ticket.origin(), ticket.destination());

        return Duration.between(departure, arrival).plus(timeZoneCorrection);
    }

    public List<Ticket> getTicketsWithOriginAndDestination(List<Ticket> tickets, String origin, String destination) {
        return tickets.stream()
                .filter(t -> t.origin().equals(origin))
                .filter(t-> t.destination().equals(destination))
                .toList();
    }

    public BigDecimal getAveragePrice(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("Tickets list is empty");
        }

        BigDecimal sum = tickets.stream()
                .map(ticket -> new BigDecimal(ticket.price()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(tickets.size()), RoundingMode.HALF_UP);
    }

    public BigDecimal getMedianaPrice(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("Tickets list is empty");
        }

        List<BigDecimal> prices = new ArrayList<>(tickets.stream()
                .map(t -> new BigDecimal(t.price()))
                .toList());
        prices.sort(Comparator.naturalOrder());

        int size = prices.size();
        if (size % 2 == 1) {
            return prices.get(size / 2);
        } else {
            BigDecimal first = prices.get(size / 2 - 1);
            BigDecimal second = prices.get(size / 2);
            return first.add(second).divide(BigDecimal.valueOf(2), RoundingMode.CEILING);
        }
    }
}
