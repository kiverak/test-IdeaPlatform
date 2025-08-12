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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketReaderServiceTest {

    @Spy
    @InjectMocks
    private TicketService ticketService;

    private MockedStatic<TicketsMapper> ticketsMapperMock;

    @BeforeEach
    void setUp() {
        // Mock the static TicketsMapper class before each test
        ticketsMapperMock = mockStatic(TicketsMapper.class);
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
}