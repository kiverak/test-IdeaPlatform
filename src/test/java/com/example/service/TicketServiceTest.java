package com.example.service;

import com.example.enums.TicketSourceType;
import com.example.mapper.TicketsMapper;
import com.example.model.Ticket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Spy
    @InjectMocks
    private TicketService ticketService;

    private MockedStatic<TicketsMapper> ticketsMapperMock;

    private List<Ticket> allTickets;
    private Ticket vvoToTlv1;
    private Ticket vvoToTlv2;
    private Ticket vvoToUfa;
    private Ticket lrnToTlv;

    @BeforeEach
    void setUp() {
        // Mock the static TicketsMapper class before each test
        ticketsMapperMock = mockStatic(TicketsMapper.class);

        vvoToTlv1 = new Ticket("VVO", "Владивосток", "TLV", "Тель-Авив", "12.05.18", "16:20", "12.05.18", "22:10", "TK", 3, "12400");
        vvoToTlv2 = new Ticket("VVO", "Владивосток", "TLV", "Тель-Авив", "12.05.18", "17:20", "12.05.18", "23:50", "S7", 1, "13100");
        vvoToUfa = new Ticket("VVO", "Владивосток", "UFA", "Уфа", "12.05.18", "15:15", "12.05.18", "17:45", "TK", 1, "33400");
        lrnToTlv = new Ticket("LRN", "Ларнака", "TLV", "Тель-Авив", "12.05.18", "12:50", "12.05.18", "14:30", "SU", 1, "7000");

        allTickets = List.of(vvoToTlv1, vvoToTlv2, vvoToUfa, lrnToTlv);
    }

    @AfterEach
    void tearDown() {
        // Close the static mock after each test to avoid state leakage
        ticketsMapperMock.close();
    }

    @Test
    @DisplayName("Should return tickets from API when source is API and JSON is valid")
    void readTickets_fromApi_success() throws IOException {
        // Arrange
        String json = """
                {
                    "origin": "VVO",
                    "origin_name": "Владивосток",
                    "destination": "TLV",
                    "destination_name": "Тель-Авив",
                    "departure_date": "12.05.18",
                    "departure_time": "16:20",
                    "arrival_date": "12.05.18",
                    "arrival_time": "22:10",
                    "carrier": "TK",
                    "stops": 3,
                    "price": 12400
                  }
                """;
        List<Ticket> expectedTickets = List.of(new Ticket("VVO", "Владивосток", "TLV",
                "Тель-Авив", "12.05.18", "16:20", "12.05.18", "22:10",
                "TK", 3, "12400"));
        doReturn(expectedTickets).when(ticketService).readTickets(json);

        // Act
        List<Ticket> actualTickets = ticketService.readTickets(json, TicketSourceType.API);

        // Assert
        assertEquals(expectedTickets, actualTickets);
        verify(ticketService).readTickets(json);
    }

    @Test
    @DisplayName("Should throw RuntimeException when API call throws IOException")
    void readTickets_fromApi_throwsIOException() throws IOException {
        // Arrange
        String json = "invalid-json";
        IllegalArgumentException illException = new IllegalArgumentException("API read error");
        doThrow(illException).when(ticketService).readTickets(json);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            ticketService.readTickets(json, TicketSourceType.API)
        );

        assertEquals(illException, exception.getCause());
    }

    @Test
    @DisplayName("Should return tickets from file when source is FILE and path is valid")
    void readTickets_fromFile_success() {
        // Arrange
        String path = "/ticket/tickets.json";
        List<Ticket> expectedTickets = List.of(new Ticket("VVO", "Владивосток", "TLV",
                "Тель-Авив", "12.05.18", "16:20", "12.05.18", "22:10",
                "TK", 3, "12400"));
        ticketsMapperMock.when(() -> TicketsMapper.readTickets(path)).thenReturn(expectedTickets);

        // Act
        List<Ticket> actualTickets = ticketService.readTickets(path, TicketSourceType.FILE);

        // Assert
        assertEquals(expectedTickets, actualTickets);
        ticketsMapperMock.verify(() -> TicketsMapper.readTickets(path));
    }

    @Test
    @DisplayName("Should throw RuntimeException when file reading throws IOException")
    void readTickets_fromFile_throwsIOException() throws IOException {
        // Arrange
        String path = "/invalid/path.json";
        IOException ioException = new IOException("File read error");
        ticketsMapperMock.when(() -> TicketsMapper.readTickets(path)).thenThrow(ioException);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            ticketService.readTickets(path, TicketSourceType.FILE)
        );

        assertEquals(ioException, exception.getCause());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when source type is null")
    void readTickets_withNullSourceType_throwsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ticketService.readTickets("some-input", null));
    }



    @Test
    @DisplayName("should return only tickets matching both origin and destination")
    void shouldReturnMatchingTickets() {
        // Given
        String origin = "VVO";
        String destination = "TLV";
        List<Ticket> expected = List.of(vvoToTlv1, vvoToTlv2);

        // When
        List<Ticket> result = ticketService.getTicketsWithOriginAndDestination(allTickets, origin, destination);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(expected));
    }

    @Test
    @DisplayName("should return an empty list when no tickets match the destination")
    void shouldReturnEmptyListWhenNoDestinationMatch() {
        // Given
        String origin = "VVO";
        String destination = "JFK"; // No flights to JFK

        // When
        List<Ticket> result = ticketService.getTicketsWithOriginAndDestination(allTickets, origin, destination);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return an empty list when no tickets match the origin")
    void shouldReturnEmptyListWhenNoOriginMatch() {
        // Given
        String origin = "JFK"; // No flights from JFK
        String destination = "TLV";

        // When
        List<Ticket> result = ticketService.getTicketsWithOriginAndDestination(allTickets, origin, destination);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return an empty list when the input list is empty")
    void shouldReturnEmptyListForEmptyInput() {
        // Given
        List<Ticket> emptyList = Collections.emptyList();
        String origin = "VVO";
        String destination = "TLV";

        // When
        List<Ticket> result = ticketService.getTicketsWithOriginAndDestination(emptyList, origin, destination);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should be case-sensitive and return an empty list for mismatched case")
    void shouldBeCaseSensitive() {
        // Given
        String origin = "vvo"; // Lowercase
        String destination = "tlv"; // Lowercase

        // When
        List<Ticket> result = ticketService.getTicketsWithOriginAndDestination(allTickets, origin, destination);

        // Then
        assertTrue(result.isEmpty());
    }
}
