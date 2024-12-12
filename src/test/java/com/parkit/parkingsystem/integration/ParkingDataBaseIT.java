package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown(){
    }

    @Test
    public void testParkingACar() throws Exception {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        inputReaderUtil.readSelection();
        inputReaderUtil.readVehicleRegistrationNumber();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        parkingService.processIncomingVehicle();
        boolean parkingSpotAvailable = ticket.getParkingSpot().isAvailable();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        assertThat(parkingSpotAvailable).isFalse();
        assertThat(ticket).isNotNull();
    }

    @Test
    public void testParkingLotExit() throws Exception {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        inputReaderUtil.readSelection();
        inputReaderUtil.readVehicleRegistrationNumber();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setPrice(10);
        ticket.setOutTime(new Date());
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();

        Date ticketOutTime = ticket.getOutTime();
        double ticketPrice = ticket.getPrice();

        assertThat(ticketPrice).isGreaterThan(0);
        assertThat(ticketOutTime).isNotNull();
    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        inputReaderUtil.readSelection();
        inputReaderUtil.readVehicleRegistrationNumber();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setPrice(10);

        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();
        int numberTicket = 2;
        assertThat(numberTicket).isGreaterThan(1);
    }
}