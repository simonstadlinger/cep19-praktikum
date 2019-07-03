package lufthansa;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Lufthansa {
    private static String baseUrl = "https://api.lufthansa.com/v1/";
    // Enter your client id here
    private static String clientId = "qbbfq6k2xra2hf5wk5nf8paf";
    // Enter your client secret here
    private static String clientSecret = "G3UzgUqbk4";
    private static String bearer = "favhwbu7aqmd59wer7tgjwjv";

    private static ArrayList<String> flightNumberBlacklist;
    private static ArrayList<String> airportBlacklist;
    private static ArrayList<String> loungeBlacklist;
    private static HashMap<String, FlightStatus> flightStatuses;
    private static HashMap<String, Airport> airports;

    static {
        bearer = requestToken();
        flightNumberBlacklist = new ArrayList<>();
        airportBlacklist = new ArrayList<>();
        loungeBlacklist = new ArrayList<>();
        flightStatuses = new HashMap<>();
        airports = new HashMap<>();
    }

    public static String getDepartureAirportCode(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getDeparture().getCode();
        }
        String statusAsJson = get("operations/flightstatus/" + flightNumber + "/" + LocalDate.now().toString());
        if (statusAsJson != null) {
            String departureAirportCode = getDepartureAirportFromJson(statusAsJson);
            if (!airports.containsKey(departureAirportCode)) {
                Airport airport = new Airport(departureAirportCode);
                airports.put(departureAirportCode, airport);
            }
            String arrivalAirportCode = getArrivalAirportFromJson(statusAsJson);
            if (!airports.containsKey(arrivalAirportCode)) {
                Airport airport = new Airport(arrivalAirportCode);
                airports.put(arrivalAirportCode, airport);
            }
            status = new FlightStatus(flightNumber, airports.get(departureAirportCode), airports.get(arrivalAirportCode));
            flightStatuses.put(flightNumber, status);
            enrichFlightStatus(status, flightNumber);
            return departureAirportCode;
        }
        flightNumberBlacklist.add(flightNumber);
        return null;
    }

    public static String getArrivalAirportCode(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getArrival().getCode();
        }
        String statusAsJson = get("operations/flightstatus/" + flightNumber + "/" + LocalDate.now().toString());
        if (statusAsJson != null) {
            String   departureAirportCode = getDepartureAirportFromJson(statusAsJson);
            if (!airports.containsKey(departureAirportCode)) {
                Airport airport = new Airport(departureAirportCode);
                airports.put(departureAirportCode, airport);
            }
            String arrivalAirportCode = getArrivalAirportFromJson(statusAsJson);
            if (!airports.containsKey(arrivalAirportCode)) {
                Airport airport = new Airport(arrivalAirportCode);
                airports.put(arrivalAirportCode, airport);
            }
            status = new FlightStatus(flightNumber, airports.get(departureAirportCode), airports.get(arrivalAirportCode));
            flightStatuses.put(flightNumber, status);
            status = enrichFlightStatus(status, flightNumber);
            return arrivalAirportCode;
        }
        flightNumberBlacklist.add(flightNumber);
        return null;
    }

    public static String getArrivalTime(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getArrivalTime();
        }
        /*String statusAsJson = get("operations/customerflightinformation/" + flightNumber + "/" + LocalDate.now().toString());
        if (statusAsJson != null) {
            return enrichNewFlightStatus(status, statusAsJson).getArrivalTime();
        }
        flightNumberBlacklist.add(flightNumber);*/
        return null;
    }

    public static String getDepartureTime(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getDepartureTime();
        }
        /*String statusAsJson = get("operations/customerflightinformation/" + flightNumber + "/" + LocalDate.now().toString());
        if (statusAsJson != null) {
            return enrichNewFlightStatus(status, statusAsJson).getDepartureTime();
        }
        flightNumberBlacklist.add(flightNumber);*/
        return null;
    }

    public static String getArrivalTerminal(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getArrivalTerminal();
        }
        return null;
    }

    public static String getDepartureTerminal(String flightNumber) {
        if (flightNumberBlacklist.contains(flightNumber)) {
            return null;
        }
        FlightStatus status = flightStatuses.get(flightNumber);
        if (status != null) {
            return status.getDepartureTerminal();
        }
        return null;
    }

    private static FlightStatus enrichFlightStatus (FlightStatus status, String flightNumber){
        String infoAsJson = get("operations/customerflightinformation/" + flightNumber + "/" + LocalDate.now().toString());
        if (infoAsJson != null) {
            String departureTime = getDepartureTimeFromJson(infoAsJson);
            status.setDepartureTime(departureTime);

            String arrivalTime = getArrivalTimeFromJson(infoAsJson);
            status.setArrivalTime(arrivalTime);

            String departureTerminal = getDepartureTerminalFromJson(infoAsJson);
            status.setDepartureTerminal(departureTerminal);

            String arrivalTerminal = getArrivalTerminalFromJson(infoAsJson);
            status.setArrivalTerminal(arrivalTerminal);
        }
        return status;
    }

    public static Object[] getDepartureAirportCoords(String flightNumber) {
        String departureAirportCode = getDepartureAirportCode(flightNumber);
        if (departureAirportCode != null) {
            if (airportBlacklist.contains(departureAirportCode)) {
                return null;
            }
            Airport airport = airports.get(departureAirportCode);
            if (airport.getCoords() != null) {
                return airport.getCoords();
            }
            String airportAsJson = get("references/airports/" + departureAirportCode);
            if (airportAsJson != null) {
                Object[] departureCoords = getDepartureCoordsFromJson(airportAsJson);
                airport.setCoords(departureCoords);
                return departureCoords;
            }
            airportBlacklist.add(departureAirportCode);
        }
        return null;
    }

    public static Object[] getArrivalAirportCoords(String flightNumber) {
        String arrivalAirportCode = getArrivalAirportCode(flightNumber);
        if (arrivalAirportCode != null) {
            if (airportBlacklist.contains(arrivalAirportCode)) {
                return null;
            }
            Airport airport = airports.get(arrivalAirportCode);
            if (airport.getCoords() != null) {
                return airport.getCoords();
            }
            String airportAsJson = get("references/airports/" + arrivalAirportCode);
            if (airportAsJson != null) {
                Object[] arrivalCoords = getArrivalCoordsFromJson(airportAsJson);
                airport.setCoords(arrivalCoords);
                return arrivalCoords;
            }
            airportBlacklist.add(flightNumber);
        }
        return null;
    }

    public static LinkedHashMap<String, Class> getArrivalAirportCoordsMetadata() {
        LinkedHashMap<String, Class> propertyNames = new LinkedHashMap<String, Class>();
        propertyNames.put("destLat", double.class);
        propertyNames.put("destLong", double.class);
        return propertyNames;
    }

    public static LinkedHashMap<String, Class> getDepartureAirportCoordsMetadata() {
        LinkedHashMap<String, Class> propertyNames = new LinkedHashMap<String, Class>();
        propertyNames.put("depLat", double.class);
        propertyNames.put("depLong", double.class);
        return propertyNames;
    }

    public static Lounge[] getAirportLounges(String airportCode) {
        if (airportCode == null) return null;
        if (loungeBlacklist.contains(airportCode)) {
            return null;
        }

        Airport airport = airports.get(airportCode);

        if (airport == null) {
            airport = new Airport(airportCode);
            airports.put(airportCode, airport);
        }
        Lounge[] loungeList = airport.getLounges();
        if (loungeList != null) {
            return loungeList;
        }
        String loungeListAsJson = get("offers/lounges/" + airportCode);
        if (loungeListAsJson != null) {
            loungeList = getLoungeListFromJson(loungeListAsJson);
            airport.setLounges(loungeList);
            return loungeList;
        }
        loungeBlacklist.add(airportCode);
        return null;
    }

    private static Lounge[] getLoungeListFromJson(String loungeListAsJson) {
        JSONObject obj = new JSONObject(loungeListAsJson);
        Lounge[] loungeList = null;
        if (((JSONObject) ((JSONObject) obj.get("LoungeResource")).get("Lounges")).get("Lounge") instanceof JSONObject) {
            JSONObject loungeObject = (JSONObject) ((JSONObject) ((JSONObject) obj.get("LoungeResource")).get("Lounges")).get("Lounge");
            loungeList = new Lounge[1];
            String name = getLoungeNameFromJson(loungeObject);
            String location = getLoungeLocationFromJson(loungeObject);
            boolean restrooms = getLoungeRestroomsFromJson(loungeObject);
            boolean showers = getLoungeShowersFromJson(loungeObject);
            boolean faxMachine = getLoungeFaxMachineFromJson(loungeObject);
            boolean wlan = getLoungeWlanFromJson(loungeObject);
            Lounge lounge = new Lounge(name, location, restrooms, showers, faxMachine, wlan);
            loungeList[0] = lounge;
        } else {
            JSONArray lounges = (JSONArray) ((JSONObject) ((JSONObject) obj.get("LoungeResource")).get("Lounges")).get("Lounge");
            loungeList = new Lounge[lounges.length()];
            for (int i = 0; i < lounges.length(); i++) {
                String name = getLoungeNameFromJson((JSONObject) lounges.get(i));
                String location = getLoungeLocationFromJson((JSONObject) lounges.get(i));
                boolean restrooms = getLoungeRestroomsFromJson((JSONObject) lounges.get(i));
                boolean showers = getLoungeShowersFromJson((JSONObject) lounges.get(i));
                boolean faxMachine = getLoungeFaxMachineFromJson((JSONObject) lounges.get(i));
                boolean wlan = getLoungeWlanFromJson((JSONObject) lounges.get(i));
                Lounge lounge = new Lounge(name, location, restrooms, showers, faxMachine, wlan);
                loungeList[i] = lounge;
            }
        }
        return loungeList;
    }

    private static boolean getLoungeWlanFromJson(JSONObject jsonObject) {
        JSONObject features = (JSONObject) jsonObject.get("Features");
        return features.getBoolean("WLANFacility");
    }

    private static boolean getLoungeFaxMachineFromJson(JSONObject jsonObject) {
        JSONObject features = (JSONObject) jsonObject.get("Features");
        return features.getBoolean("FaxMachine");
    }

    private static boolean getLoungeShowersFromJson(JSONObject jsonObject) {
        JSONObject features = (JSONObject) jsonObject.get("Features");
        return features.getBoolean("ShowerFacilities");
    }

    private static boolean getLoungeRestroomsFromJson(JSONObject jsonObject) {
        JSONObject features = (JSONObject) jsonObject.get("Features");
        return features.getBoolean("Restrooms");
    }

    private static String getLoungeLocationFromJson(JSONObject jsonObject) {
        if (!jsonObject.has("Locations")) return null;
        if (((JSONObject) jsonObject.get("Locations")).get("Location") instanceof JSONObject) {
            JSONObject name = (JSONObject) ((JSONObject) jsonObject.get("Locations")).get("Location");
            return name.getString("$");
        } else {
            JSONArray locationMultiLang = (JSONArray) ((JSONObject) jsonObject.get("Locations")).get("Location");
            for (int i = 0; i < locationMultiLang.length(); i++) {
                if (((JSONObject) locationMultiLang.get(i)).getString("@LanguageCode").equalsIgnoreCase("en"))
                    return ((JSONObject) locationMultiLang.get(i)).getString("$");
            }
            return ((JSONObject) locationMultiLang.get(0)).getString("$");
        }
    }

    private static String getLoungeNameFromJson(JSONObject jsonObject) {
        if (((JSONObject) jsonObject.get("Names")).get("Name") instanceof JSONObject) {
            JSONObject name = (JSONObject) ((JSONObject) jsonObject.get("Names")).get("Name");
            return name.getString("$");
        } else {
            JSONArray name = (JSONArray) ((JSONObject) jsonObject.get("Names")).get("Name");
            return ((JSONObject) name.get(0)).getString("$");
        }
    }

    private static String getArrivalAirportFromJson(String statusAsJson) {
        JSONObject obj = new JSONObject(statusAsJson);
        if (((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight") instanceof JSONObject) {
            JSONObject flight = (JSONObject) ((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight");
            return ((JSONObject) flight.get("Arrival")).getString("AirportCode");
        } else {
            JSONArray flight = (JSONArray) ((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight");
            return ((JSONObject) ((JSONObject) flight.get(0)).get("Arrival")).getString("AirportCode");
        }
    }

    private static String getDepartureAirportFromJson(String statusAsJson) {
        JSONObject obj = new JSONObject(statusAsJson);
        if (((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight") instanceof JSONObject) {
            JSONObject flight = (JSONObject) ((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight");
            return ((JSONObject) flight.get("Departure")).getString("AirportCode");
        } else {
            JSONArray flight = (JSONArray) ((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight");
            return ((JSONObject) ((JSONObject) flight.get(0)).get("Departure")).getString("AirportCode");
        }
    }

    private static String getArrivalTimeFromJson(String statusAsJson) {
        JSONObject obj = new JSONObject(statusAsJson);
        if (((JSONObject) ((JSONObject) obj.get("FlightInformation")).get("Flights")).get("Flight") instanceof JSONObject) {
            JSONObject flight = (JSONObject) ((JSONObject) ((JSONObject) obj.get("FlightInformation")).get("Flights")).get("Flight");
            return ((JSONObject) ((JSONObject) flight.get("Arrival")).get("Actual")).getString("Time");
        } else {
            JSONArray flight = (JSONArray) ((JSONObject) ((JSONObject) obj.get("FlightInformation")).get("Flights")).get("Flight");
            return ((JSONObject) ((JSONObject) ((JSONObject) flight.get(0)).get("Arrival")).get("Actual")).getString("Time");
        }
    }

    private static String getDepartureTimeFromJson(String statusAsJson) {
        JSONObject obj = new JSONObject(statusAsJson);
        if (((JSONObject) ((JSONObject) obj.get("FlightInformation")).get("Flights")).get("Flight") instanceof JSONObject) {
            JSONObject flight = (JSONObject) ((JSONObject) ((JSONObject) obj.get("FlightInformation")).get("Flights")).get("Flight");
            return ((JSONObject) ((JSONObject) flight.get("Departure")).get("Actual")).getString("Time");
        } else {
            JSONArray flight = (JSONArray) ((JSONObject) ((JSONObject) obj.get("FlightInformation")).get("Flights")).get("Flight");
            return ((JSONObject) ((JSONObject) ((JSONObject) flight.get(0)).get("Departure")).get("Actual")).getString("Time");
        }
    }

    // TODO - handle nonexistence of Terminal
    private static String getArrivalTerminalFromJson(String statusAsJson) {
        JSONObject obj = new JSONObject(statusAsJson);
        if (((JSONObject) ((JSONObject) obj.get("FlightInformation")).get("Flights")).get("Flight") instanceof JSONObject) {
            JSONObject flight = (JSONObject) ((JSONObject) ((JSONObject) obj.get("FlightInformation")).get("Flights")).get("Flight");
            return String.valueOf(((JSONObject) flight.get("Arrival")).get("Terminal"));
        } else {
            JSONArray flight = (JSONArray) ((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight");
            return String.valueOf(((JSONObject) ((JSONObject) flight.get(0)).get("Arrival")).get("Terminal"));
        }
    }

    // TODO - handle nonexistence of Terminal
    private static String getDepartureTerminalFromJson(String statusAsJson) {
        JSONObject obj = new JSONObject(statusAsJson);
        if (((JSONObject) ((JSONObject) obj.get("FlightInformation")).get("Flights")).get("Flight") instanceof JSONObject) {
            JSONObject flight = (JSONObject) ((JSONObject) ((JSONObject) obj.get("FlightInformation")).get("Flights")).get("Flight");
            return String.valueOf(((JSONObject) flight.get("Departure")).get("Terminal"));
        } else {
            JSONArray flight = (JSONArray) ((JSONObject) ((JSONObject) obj.get("FlightStatusResource")).get("Flights")).get("Flight");
            return String.valueOf(((JSONObject) ((JSONObject) flight.get(0)).get("Departure")).get("Terminal"));
        }
    }

    private static Object[] getArrivalCoordsFromJson(String statusAsJson) {
        JSONObject obj = new JSONObject(statusAsJson);
        JSONObject airport = (JSONObject) ((JSONObject) ((JSONObject) obj.get("AirportResource")).get("Airports")).get("Airport");
        JSONObject coords = (JSONObject) ((JSONObject) airport.get("Position")).get("Coordinate");
        Object[] latLong = {coords.getDouble("Latitude"), coords.getDouble("Longitude")};
        return latLong;
    }

    private static Object[] getDepartureCoordsFromJson(String statusAsJson) {
        JSONObject obj = new JSONObject(statusAsJson);
        JSONObject airport = (JSONObject) ((JSONObject) ((JSONObject) obj.get("AirportResource")).get("Airports")).get("Airport");
        JSONObject coords = (JSONObject) ((JSONObject) airport.get("Position")).get("Coordinate");
        Object[] latLong = {coords.getDouble("Latitude"), coords.getDouble("Longitude")};
        return latLong;
    }

    private static String get(String urlSuffix) {
        String assembledOutput = "";
        try {
            URL urlObject = new URL(baseUrl + urlSuffix);
            HttpsURLConnection connection = (HttpsURLConnection) urlObject.openConnection();


            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/" + "json");
            connection.setRequestProperty("authorization", "Bearer " + bearer);

            connection.connect();

            if (connection.getResponseCode() == 200) {


                BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                        (connection.getInputStream())));

                String output;
                while ((output = responseBuffer.readLine()) != null) {
                    assembledOutput = assembledOutput + output;
                }
            } else {
                throw new IOException("Failed with HTTP code " + connection.getResponseCode() + " calling " + baseUrl + urlSuffix);
            }
            connection.disconnect();
        } catch (IOException e) {
            System.err.println(e);
            return null;
        }

        return assembledOutput;
    }

    private static String requestToken() {
        String assembledOutput = "";

        try {
            URL urlObject = new URL(baseUrl + "oauth/token");
            HttpsURLConnection connection = (HttpsURLConnection) urlObject.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String urlParameters = "client_id=" + clientId + "&client_secret=" + clientSecret + "&grant_type=client_credentials";
            byte[] postData = urlParameters.getBytes();

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                    (connection.getInputStream())));
            String output;
            while ((output = responseBuffer.readLine()) != null) {
                assembledOutput = assembledOutput + output;
            }
        } catch (IOException e) {
            System.err.println(e);
            return null;
        }
        JSONObject obj = new JSONObject(assembledOutput);
        return obj.getString("access_token");
    }
}
