package com.example.mapper;

import com.example.model.Ticket;
import com.example.model.TicketsWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class TicketsMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Reads a JSON file from the resources folder, parses it, and returns a list of tickets.
     *
     * @param resourcePath The path to the JSON file within the resources directory (e.g., "ticket/tickets.json").
     * @return A list of {@link Ticket} objects.
     * @throws IOException if the file cannot be read or parsed.
     */
    public static List<Ticket> readTickets(String resourcePath) throws IOException {
        try (InputStream inputStream = TicketsMapper.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Cannot find resource file: " + resourcePath);
            }
            TicketsWrapper wrapper = objectMapper.readValue(inputStream, TicketsWrapper.class);
            return wrapper.tickets() != null ? wrapper.tickets() : Collections.emptyList();
        }
    }
}
