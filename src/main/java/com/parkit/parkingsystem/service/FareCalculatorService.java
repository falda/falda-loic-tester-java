package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket,  Boolean discount){

        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long outTime = ticket.getOutTime().getTime();
        long inTime = ticket.getInTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        long difference = outTime - inTime;
        double duration = (double) ((difference / 1000) / 60) /60;

        if (duration < 0.5) {
            duration = 0;
        }

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if(discount) {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * 0.95);
                } else {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                }
                break;
            }
            case BIKE: {
                if (discount) {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * 0.95);
                } else {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                }
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}