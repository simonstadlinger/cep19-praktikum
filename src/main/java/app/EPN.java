package app;

import booking.Booking;
import booking.CabinClass;
import cep.CEPListener;
import com.espertech.esper.client.*;
import com.google.gson.JsonSyntaxException;
import com.github.javafaker.Faker;
import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;

import cities.Cities;
import org.opensky.api.OpenSkyApi;
import org.opensky.model.OpenSkyStates;
import org.opensky.model.StateVector;
import utils.Callsign;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class EPN {

    private static EPRuntime cepRT;
    private static Faker faker = new Faker();

    public static void main(String[] args) throws APIException {

        // setup
        EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine");
        cep.initialize();
        EPAdministrator cepAdm = cep.getEPAdministrator();
        ConfigurationOperations cp = cepAdm.getConfiguration();
        cepRT = cep.getEPRuntime();

        // event types
        cp.addEventType("StateVector", StateVector.class.getName());
        cp.addEventType("CurrentWeather", CurrentWeather.class.getName());
        cp.addEventType("Booking", Booking.class.getName());

        // event queries (EPAs)
        EPStatement lhFilter = cepAdm.createEPL("insert into OutStream1 select * from StateVector(callsign regexp '[ \\t\\n\\f\\r]*(EWG|DLH|AUA|SWR)[0-9]{1,4}[ \\t\\n\\f\\r]*')");

        EPStatement callsignToFlightNumber = cepAdm.createEPL("insert into OutStream2 select *, utils.Callsign.icaoToIata(callsign) as flightNumber from OutStream1");

        EPStatement lhDestinationAirport = cepAdm.createEPL("insert into OutStream3 select *, lufthansa.Lufthansa.getArrivalAirportCode(flightNumber) as destinationAirport from OutStream2");

        EPStatement infoCompose = cepAdm.createEPL("insert into OutStream4 select * from OutStream3.win:length(5) as o3, Booking.win:length(5) as b where o3.flightNumber = b.flightNumber");

        EPStatement enrichETA = cepAdm.createEPL("insert into EnrichedStream1 select *, " +
                "utils.GeoUtils.eta(utils.GeoUtils.distance(cast(lufthansa.Lufthansa.getArrivalAirportCoords(o3.flightNumber).get(0), double), cast(lufthansa.Lufthansa.getArrivalAirportCoords(o3.flightNumber).get(1), double), " +
                "o3.latitude, o3.longitude), o3.velocity) as eta from OutStream4");

        EPStatement enrichCities = cepAdm.createEPL("insert into FullyEnrichedStream select *, " +
                "cities.Cities.getCity(o3.latitude, o3.longitude) as city from EnrichedStream1");

        EPStatement ecoPassenger = cepAdm.createEPL("insert into OutStream6 select * from FullyEnrichedStream where b.cabinClass = booking.CabinClass.ECONOMY");

        EPStatement noEcoPassenger = cepAdm.createEPL("insert into OutStream5 select * from FullyEnrichedStream where b.cabinClass !=booking.CabinClass.ECONOMY");

        EPStatement loungeInfo = cepAdm.createEPL("insert into OutStream8 select *, lufthansa.Lufthansa.getAirportLounges(o3.destinationAirport) as lounges from OutStream5");

        EPStatement loungeSelector = cepAdm.createEPL("insert into OutStream9 select o3.flightNumber, o3.destinationAirport, " +
                "lounges[0].name as loungeName, lounges[0].showers as showers from OutStream8");

        EPStatement ifeLounge = cepAdm.createEPL("insert into PremiumFinalStream select * from OutStream9");

        EPStatement ifeNoLounge = cepAdm.createEPL("insert into FinalStream select * from OutStream6");

        // event listener
        lhFilter.addListener(new CEPListener("lhFilter"));
        callsignToFlightNumber.addListener(new CEPListener("callsignToFlightNumber"));
        lhDestinationAirport.addListener(new CEPListener("lhDestinationAirport"));
        infoCompose.addListener(new CEPListener("infoCompose"));
        enrichETA.addListener(new CEPListener("enrichETA"));
        enrichCities.addListener(new CEPListener("enrichCities"));
        ecoPassenger.addListener(new CEPListener("ecoPassenger"));
        noEcoPassenger.addListener(new CEPListener("noEcoPassenger"));
        loungeInfo.addListener(new CEPListener("loungeInfo"));
        loungeSelector.addListener(new CEPListener("loungeSelector"));
        ifeNoLounge.addListener(new CEPListener("ife"));
        ifeLounge.addListener(new CEPListener("ifeLounge"));

        // send events to engine
        Thread thread1 = new Thread() {
            public void run() {
                OpenSkyApi opensky = new OpenSkyApi();
                sendOpenSkyEvents(opensky);
            }
        };
        Thread thread2 = new Thread() {
            public void run() {
                // Enter your OWM key here
                String owmKey = "5dc29b69d310acbbaa43440bc1382d75";
                OWM owm = new OWM(owmKey);
                owm.setUnit(OWM.Unit.METRIC);
                sendWeatherEvents(owm);
            }
        };

        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sendOpenSkyEvents(OpenSkyApi opensky) {
        for (int i = 0; i < 100; i++) {
            OpenSkyStates os = null;
            try {
                os = opensky.getStates(0, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (StateVector flight : os.getStates()) {
                if (flight.getLatitude() != null) {
                    cepRT.sendEvent(flight);
                    String flightNumber = "";
                    try {
                        flightNumber = Callsign.icaoToIata(flight.getCallsign());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendBookingEvents(flightNumber);
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void sendBookingEvents(String flightNumber) {
        for (int i = 0; i < 10; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 4);
            cepRT.sendEvent(new Booking(flightNumber, CabinClass.values()[randomNum], faker.name().fullName()));
        }
    }

    public static void sendWeatherEvents(OWM owm) {

        ArrayList<String[]> cities = Cities.getCities();
        for (int j = 0; j < 100; j++) {
            for (int i = 0; i < cities.size(); i++) {
                String[] city = cities.get(i);
                String cityName = city[0];
                CurrentWeather cwd = null;
                try {
                    cwd = owm.currentWeatherByCityName(cityName);
                } catch (APIException e) {
                    e.printStackTrace();
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
                if (cwd != null) {
                    cepRT.sendEvent(cwd);
                }
            }
            try {
                Thread.sleep(120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
