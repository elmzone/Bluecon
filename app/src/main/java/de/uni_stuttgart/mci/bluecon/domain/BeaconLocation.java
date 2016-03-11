package de.uni_stuttgart.mci.bluecon.domain;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeaconLocation  implements Comparable<BeaconLocation>{
    public static final String NO_NEXT_BEACON = "noNext";
    public String macAddress = "macaddress";
    public int RSSI;
    public String UUID = "UUID";
    public String type = "type";
    public String id = "id";
    public String status = "status";
    public String placeId = "placeId";
    public String roomId = "roomId";   public String latitude = "latitude";  public String longitude = "longitude";
    public String expectedStability = "expectedStability";
    public String description = "description";
    public Map<String, Neighbor> neighborhood =new HashMap<>();
    public String nextBeacon = NO_NEXT_BEACON;

    public int compareTo(@NonNull BeaconLocation another) {
        int thisRSSI = this.RSSI;
        int anotherRSSI = another.RSSI;
        if (thisRSSI < anotherRSSI) {
            return 1;
        }
        else if (thisRSSI > anotherRSSI) {
            return -1;
        }
        else  {
            return 0;
        }
    }

}
