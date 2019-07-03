package booking;

public class Booking {
    private String flightNumber;
    private CabinClass cabinClass;
    private String passengerName;
    private Object connectingFlight;

    public Booking(String flightNumber, CabinClass cabinClass, String passengerName) {
        this.flightNumber = flightNumber;
        this.cabinClass = cabinClass;
        this.passengerName = passengerName;
    }

    public Booking(String flightNumber, Object connectingFlight){
        this.flightNumber = flightNumber;
        this.connectingFlight = connectingFlight;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public Object getConnectingFlight(){return connectingFlight;}

    public void setConnectingFlight(Object connectingFlight) { this.connectingFlight = connectingFlight; }

    @Override
    public String toString() {
        return "Booking{" +
                "flightNumber='" + flightNumber + '\'' +
                ", cabinClass=" + cabinClass +
                ", passengerName='" + passengerName + '\'' +
                '}';
    }
}
