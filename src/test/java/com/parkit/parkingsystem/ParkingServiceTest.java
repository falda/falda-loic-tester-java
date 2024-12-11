package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        try {
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,true);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.getNbTicket(any(Ticket.class))).thenReturn(1);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock object");
        }
        parkingService.processExitingVehicle();

        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertThat(ticket).isInstanceOf(Ticket.class);

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

        ParkingSpot parkingSpot = new ParkingSpot(2, ParkingType.CAR, false);
        Boolean parkingSpotResult = parkingSpotDAO.updateParking(parkingSpot);
        assertThat(parkingSpotResult).isTrue();

        verify(ticketDAO, Mockito.times(1)).getNbTicket(any(Ticket.class));

        int ticketResult = ticketDAO.getNbTicket(ticketDAO.getTicket("ABCDEF"));
        assertThat(ticketResult).isEqualTo(1);
    }

    @Test
    public void processIncomingVehicleTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
            when(ticketDAO.getNbTicket(any(Ticket.class))).thenReturn(1);
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock object");
        }

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

        ParkingSpot parkingSpot = new ParkingSpot(2, ParkingType.CAR, false);
        Boolean parkingSpotResult = parkingSpotDAO.updateParking(parkingSpot);
        assertThat(parkingSpotResult).isFalse();

        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));

        Boolean saveTicketResult = ticketDAO.saveTicket(ticketDAO.getTicket("ABCDEF"));
        assertThat(saveTicketResult).isFalse();

        verify(ticketDAO, times(1)).getNbTicket(any(Ticket.class));

        Ticket ticket = new Ticket();
        int getNbTicketResult = ticketDAO.getNbTicket(ticket);
        assertThat(getNbTicketResult).isEqualTo(1);
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setId(1);
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setOutTime(new Date(System.currentTimeMillis()));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            parkingService.processExitingVehicle();

            verify(ticketDAO, Mockito.times(1)).getTicket(anyString());

            assertThat(ticket).isInstanceOf(Ticket.class);

            verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock object");
        }
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(parkingSpot.getId());

        parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));

        int parkingSpotDAONextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertThat(parkingSpotDAONextAvailableSlot).isEqualTo(1);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

        parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));

        int parkingSpotDAONextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertThat(parkingSpotDAONextAvailableSlot).isEqualTo(0);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        when(inputReaderUtil.readSelection()).thenReturn(3);

        parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpotDAO, Mockito.times(0)).getNextAvailableSlot(any(ParkingType.class));

        int parkingSpotDAONextAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertThat(parkingSpotDAONextAvailableSlot).isEqualTo(0);
    }
}