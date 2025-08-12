package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A wrapper class to match the root structure of the tickets.json file,
 * which contains a "tickets" key with a list of ticket objects.
 *
 * @param tickets The list of tickets.
 */
public record TicketsWrapper(
        @JsonProperty("tickets") List<Ticket> tickets
) {
}
