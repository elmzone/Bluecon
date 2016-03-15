package de.uni_stuttgart.mci.bluecon.util2;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.uni_stuttgart.mci.bluecon.domain.BeaconLocation;
import de.uni_stuttgart.mci.bluecon.domain.LocationInfo;
import de.uni_stuttgart.mci.bluecon.domain.Neighbor;

/**
 * Created by florian on 03.12.15.
 */
public class ParserUtil {

    private final static String rootObjectName = "data";
    private final static String TAG = "jsonParserUtil";

    // basic function
    // returns List of locations out of inputStream
    public static List<BeaconLocation> parseLocation(InputStream in) throws IOException {
        JsonReader jsonReader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        jsonReader.setLenient(true);
        try {
            return readRootObject(jsonReader);
        } finally {
            jsonReader.close();
        }

    }

    private static List<BeaconLocation> readRootObject(JsonReader jsonReader) {
        List<BeaconLocation> locations = null;
        try {
            jsonReader.beginObject();
            if (jsonReader.hasNext()) { //only do once
                Log.d(TAG, "next is" + jsonReader.peek());
                String name = jsonReader.nextName();
                if (name.equals(rootObjectName)) {
                    locations = readLocationsArray(jsonReader);
                }
            }
            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error happened in read the root object");
        }
        return locations;
    }

    //read the location array
    private static List<BeaconLocation> readLocationsArray(JsonReader jsonReader) {
        List<BeaconLocation> locations = new ArrayList<>();
        try {
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                locations.add(readMacAddress(jsonReader));
            }
            jsonReader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error happened in read the array");
        }
        return locations;
    }

    //read the object with only one property : macAddress
    private static BeaconLocation readMacAddress(JsonReader jsonReader) {
        BeaconLocation location = new BeaconLocation();
        try {
            jsonReader.beginObject();
            if (jsonReader.peek() != JsonToken.NULL) {
                location.macAddress = jsonReader.nextName();
                Log.d(TAG, "read new location info with mac address" + location.macAddress);
                readLocation(jsonReader, location);
            }
            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return location;
    }

    //read the location information properties.
    private static BeaconLocation readLocation(JsonReader jsonReader, BeaconLocation location) {
        try {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                if (jsonReader.peek() != JsonToken.NULL) {
                    String name = jsonReader.nextName();
                    switch (name) {
                        case "type":
                            location.type = jsonReader.nextString();
                            break;
                        case "id":
                            location.id = jsonReader.nextString();
                            break;
                        case "status":
                            location.status = jsonReader.nextString();
                            break;
                        case "placeId":
                            location.placeId = jsonReader.nextString();
                            break;
                        case "roomId":
                            location.roomId = jsonReader.nextString();
                            break;
                        case "latitude":
                            if (!JsonToken.NULL.equals(jsonReader.peek())) {

                                location.latitude = jsonReader.nextString();
                            }
                            break;
                        case "longitude":
                            if (!JsonToken.NULL.equals(jsonReader.peek())) {
                                location.longitude = jsonReader.nextString();
                            }
                            break;
                        case "expectedStability":
                            location.expectedStability = jsonReader.nextString();
                            break;
                        case "description":
                            location.description = jsonReader.nextString();
                            break;
                        case "neighborhood":
                            readNeighbors(jsonReader, location);
                            break;
                        default:
                            jsonReader.skipValue();
                    }
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error happened in read macAddress object");
        }
        return location;
    }//read the object with only one property : macAddress

    private static BeaconLocation readNeighbors(JsonReader jsonReader, BeaconLocation location) {
        try {
            if (JsonToken.BEGIN_ARRAY.equals(jsonReader.peek())) {
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    String neighborString = jsonReader.nextString();
                    int separator = neighborString.indexOf(':');
                    Neighbor neighbor = new Neighbor();
                    neighbor.roomId = neighborString.substring(0, separator);
                    neighbor.wayToIt = neighborString.substring(separator + 1, neighborString.length());
                    location.neighborhood.put(neighbor.roomId, neighbor);
                }
                jsonReader.endArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return location;
    }
}
