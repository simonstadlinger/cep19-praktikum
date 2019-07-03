package lufthansa;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

class FlightStatus {
    private String flightNumber;
    private Airport departure;
    private Airport arrival;
    private String departureTime;
    private String arrivalTime;
    private String departureTerminal;
    private String arrivalTerminal;


    public FlightStatus(String flightNumber, Airport departure, Airport arrival) {
        if (departure.equals(null)) {departure = new Airport("FRA");}
        if (arrival.equals(null)) {arrival = new Airport("FRA");}

        this.flightNumber = flightNumber;
        this.departure = departure;
        this.arrival = arrival;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public Airport getDeparture() {
        return departure;
    }

    public void setDeparture(Airport departure) {
        this.departure = departure;
    }

    public Airport getArrival() {
        return arrival;
    }

    public void setArrival(Airport arrival) {
        this.arrival = arrival;
    }

    public String getDepartureTime(){
        return departureTime;
    }

    public void setDepartureTime(String departureTime){
        this.departureTime = departureTime;
    }

    public String getArrivalTime(){
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime){
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureTerminal(){
        return departureTerminal;
    }

    public void setDepartureTerminal(String departureTerminal){
        this.departureTerminal = departureTerminal;
    }

    public String getArrivalTerminal(){
        return arrivalTerminal;
    }

    public void setArrivalTerminal(String arrivalTerminal){
        this.arrivalTerminal = arrivalTerminal;
    }

}
