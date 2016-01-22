package de.uni_stuttgart.mci.bluecon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by florian on 20.01.16.
 */
public class BeaconHolder {

    private static BeaconHolder inst;


    private List<BeaconsInfo> currentBeacons = new ArrayList<>();
    private List<BeaconListener> beaconListener = new ArrayList<>();

    private BeaconHolder() {
        inst = this;
    }

    public static BeaconHolder inst() {
        if (inst == null) {
            new BeaconHolder();
        }
        return inst;
    }

    public List<BeaconsInfo> getCurrentBeacons() {
        return currentBeacons;
    }

    public static List<BeaconsInfo> beacons() {
        return inst().currentBeacons;
    }

    public boolean addBeacons(BeaconsInfo... beaconsInfos) {
        boolean result = Collections.addAll(currentBeacons, beaconsInfos);
        if (result) {
            for (BeaconListener bl : beaconListener) {
                bl.onBeaconsAdded();
            }
        }
        return result;
    }

    public boolean addBeacons(List<BeaconsInfo> beaconsInfos) {
        boolean result = currentBeacons.addAll(beaconsInfos);
        if (result) {
            for (BeaconListener bl : beaconListener) {
                bl.onBeaconsAdded();
            }
        }
        return result;
    }

    public void setCurrentBeacons(List<BeaconsInfo> currentBeacons) {
        this.currentBeacons = currentBeacons;
        for (BeaconListener l : beaconListener) {
            l.onBeaconsChanged(currentBeacons);
        }
    }

    public void replaceBeacon(BeaconsInfo info) {
        for (BeaconsInfo beaconsInfo : currentBeacons) {
            if (beaconsInfo.macAddress == info.macAddress) {
                beaconsInfo.RSSI = info.RSSI;
            }
        }
        for (BeaconListener l : beaconListener) {
            l.onBeaconsChanged(null);
        }
    }

    public void registerBeaconListener(BeaconListener listener) {
        this.beaconListener.add(listener);
    }

    public void deregisterBeaconListener(BeaconListener listener) {
        this.beaconListener.remove(listener);
    }

    public static interface BeaconListener {

        void onBeaconsChanged(List<BeaconsInfo> changedBeacons);

        void onBeaconsAdded();

        void onBeaconsRemoved(List<BeaconsInfo> removedBeacons);
    }
}
