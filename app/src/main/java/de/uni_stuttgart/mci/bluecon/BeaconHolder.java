package de.uni_stuttgart.mci.bluecon;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_stuttgart.mci.bluecon.domain.BeaconLocation;
import de.uni_stuttgart.mci.bluecon.domain.BeaconsInfo;
import de.uni_stuttgart.mci.bluecon.domain.LocationInfo;

/**
 * Created by florian on 20.01.16.
 */
public class BeaconHolder {

    private static final String TAG = "BeaconHolder" ;
    private static BeaconHolder inst;


    private List<BeaconsInfo> currentBeacons = new ArrayList<>();
    private Map<String,BeaconLocation> currentBeaconsMap = new HashMap<>();
    private List<IBeaconListener> IBeaconListener = new ArrayList<>();
    private List<LocationInfo> locationInfo;
    private List<BeaconLocation> beaconLocations = new ArrayList<>();

    private BeaconHolder() {
        inst = this;
    }

    public static BeaconHolder inst() {
        if (inst == null) {
            new BeaconHolder();
        }
        return inst;
    }

    public List<BeaconLocation> getCurrentBeacons() {
        return new ArrayList<BeaconLocation>( currentBeaconsMap.values());
    }

    public static List<BeaconLocation> beacons() {
        return inst().getCurrentBeacons();
    }

    public static List<LocationInfo> locations() {
        return inst().locationInfo;
    }

    public static List<BeaconLocation> beaconLocations() {
        return inst().beaconLocations;
    }

    public boolean addBeacons(BeaconsInfo... beaconsInfos) {
        boolean result = Collections.addAll(currentBeacons, beaconsInfos);
        if (result) {
            for (IBeaconListener bl : IBeaconListener) {
                bl.onBeaconsAdded();
            }
        }
        return result;
    }

    public boolean addBeacons(List<BeaconsInfo> beaconsInfos) {
        boolean result = currentBeacons.addAll(beaconsInfos);

        if (result) {
            for (IBeaconListener bl : IBeaconListener) {
                bl.onBeaconsAdded();
            }
        }
        return result;
    }


    public boolean addBeaconLocations(List<BeaconLocation> beaconsLocations) {
        boolean result = beaconLocations.addAll(beaconsLocations);
        return result;
    }

    public void setCurrentBeacons(List<BeaconsInfo> currentBeacons) {
        this.currentBeacons = currentBeacons;
        for (IBeaconListener l : IBeaconListener) {
            l.onBeaconsChanged(currentBeacons);
        }
    }

    public void replaceBeacon(BeaconsInfo info) {
        for (BeaconsInfo beaconsInfo : currentBeacons) {
            if (beaconsInfo.macAddress == info.macAddress) {
                beaconsInfo.RSSI = info.RSSI;
            }
        }
        for (IBeaconListener l : IBeaconListener) {
            l.onBeaconsChanged(null);
        }
    }

    public void registerBeaconListener(IBeaconListener listener) {
        this.IBeaconListener.add(listener);
    }

    public void deregisterBeaconListener(IBeaconListener listener) {
        this.IBeaconListener.remove(listener);
    }

    public void addLocationInfo(List<LocationInfo> locationInfos) {
        this.locationInfo = locationInfos;
    }

    public void handleNewBeacon(ScanResult result) {
        if (!currentBeaconsMap.containsKey(result.getDevice().getAddress())) {
            int receiveRSSI = result.getRssi();
            BluetoothDevice receiveBeacon = result.getDevice();

            String deviceMAC = receiveBeacon.getAddress();
            Log.d(TAG, "macAddress from API 21 is" + deviceMAC);
                ScanRecord receiveRecord = result.getScanRecord();

                String deviceName = "NULL NAME";
                String mServiceName = receiveBeacon.getName();
                if (mServiceName != null) {
                    deviceName = mServiceName;
                }
                List<ParcelUuid> mServiceUUID = receiveRecord.getServiceUuids();
                String bleUUID = "NULL UUID";
                if (mServiceUUID != null) {
                    bleUUID = receiveRecord.getServiceUuids().toString();
                }
                Log.d("recordInfo", receiveRecord.toString());

                BeaconLocation beaconLocation = new BeaconLocation();
                beaconLocation.placeId = deviceName;
                beaconLocation.RSSI = receiveRSSI;
                beaconLocation.macAddress = deviceMAC;
                beaconLocation.description = bleUUID;

                currentBeaconsMap.put(deviceMAC, beaconLocation);

        }
    }


    public static interface IBeaconListener {

        void onBeaconsChanged(List<BeaconsInfo> changedBeacons);

        void onBeaconsAdded();

        void onBeaconsRemoved(List<BeaconsInfo> removedBeacons);
    }
}
