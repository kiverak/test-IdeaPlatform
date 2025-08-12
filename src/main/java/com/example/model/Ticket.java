package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single flight ticket.
 * <p>
 *
 * @param origin          The IATA code for the origin airport.
 * @param originName      The name of the origin city.
 * @param destination     The IATA code for the destination airport.
 * @param destinationName The name of the destination city.
 * @param departureDate   The departure date in "dd.MM.yy" format.
 * @param departureTime   The departure time in "HH:mm" format.
 * @param arrivalDate     The arrival date in "dd.MM.yy" format.
 * @param arrivalTime     The arrival time in "HH:mm" format.
 * @param carrier         The IATA code for the carrier.
 * @param stops           The number of stops.
 * @param price           The price of the ticket.
 */

public record Ticket(
        String origin,
        @JsonProperty("origin_name") String originName,
        String destination,
        @JsonProperty("destination_name") String destinationName,
        @JsonProperty("departure_date") String departureDate,
        @JsonProperty("departure_time") String departureTime,
        @JsonProperty("arrival_date") String arrivalDate,
        @JsonProperty("arrival_time") String arrivalTime,
        String carrier,
        int stops,
        String price
) {
}
