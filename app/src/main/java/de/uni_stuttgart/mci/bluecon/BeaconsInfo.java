package de.uni_stuttgart.mci.bluecon;

import android.support.annotation.NonNull;

public class BeaconsInfo implements Comparable<BeaconsInfo>{
    public String name;
    public int RSSI;
    public String UUID;
    public String macAddress;

    public String toString() { return "name:" + name + ";RSSI:" + RSSI + ";category:" + UUID; }

    public int compareTo(@NonNull BeaconsInfo another) {
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
