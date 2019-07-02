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
    private String departureDate;
    private String arrivalDate;
    private String departureGate;
    private String arrivalGate;
    private String departureTerminal;
    private String arrivalTerminal;


    public FlightStatus(String flightNumber, Airport departure, Airport arrival) {
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

    public String getDepartureDate(){
        return departureDate;
    }

    public void setDepartureDate(String departureDate){
        this.departureDate = departureDate;
    }

    public String getArrivalDate(){
        return arrivalDate;
    }

    public void setArrivalDate(String arrivalDate){
        this.arrivalDate = arrivalDate;
    }

    public String getDepartureGate(){
        return departureGate;
    }

    public void setDepartureGate(String departureGate){
        this.departureGate = departureGate;
    }

    public String getArrivalGate(){
        return arrivalGate;
    }

    public void setArrivalGate(String arrivalGate){
        this.arrivalGate = arrivalGate;
    }

    public String getDepartureTerminal(){
        return departureTerminal;
    }

    public void setDepartureTerminal(String departureTerminale){
        this.departureTerminal = departureTerminal;
    }

    public String getArrivalTerminal(){
        return arrivalTerminal;
    }

    public void setArrivalTerminal(String arrivalTerminal){
        this.arrivalTerminal = arrivalTerminal;
    }

}
