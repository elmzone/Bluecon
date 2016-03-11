package de.uni_stuttgart.mci.bluecon;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
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

    private static final String TAG = "BeaconHolder";
    private static BeaconHolder inst;


    private Map<String, BeaconLocation> currentBeaconsMap = new HashMap<>();
    private List<IBeaconListener> beaconListener = new ArrayList<>();
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
        return new ArrayList<BeaconLocation>(currentBeaconsMap.values());
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


    public boolean addBeaconLocations(List<BeaconLocation> beaconsLocations) {
        boolean result = beaconLocations.addAll(beaconsLocations);
        return result;
    }


    public void registerBeaconListener(IBeaconListener listener) {
        this.beaconListener.add(listener);
    }

    public void deregisterBeaconListener(IBeaconListener listener) {
        this.beaconListener.remove(listener);
    }

    public void addLocationInfo(List<LocationInfo> locationInfos) {
        this.locationInfo = locationInfos;
    }

    public void handleNewBeacon(ScanResult result) {

        if (!currentBeaconsMap.containsKey(result.getDevice().getAddress())) {
            BeaconLocation beaconLocation = new BeaconLocation();

            int receiveRSSI = result.getRssi();
            BluetoothDevice receiveBeacon = result.getDevice();

            String deviceMAC = receiveBeacon.getAddress();
//            deviceMAC = deviceMAC.replace(":", "");
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
            beaconLocation.placeId = deviceName;
            beaconLocation.macAddress = deviceMAC;

            for (BeaconLocation b : beaconLocations) {
                if (compare(b.macAddress, deviceMAC)) {
                    beaconLocation = b;
                    break;
                }

            }

            beaconLocation.RSSI = receiveRSSI;
            beaconLocation.UUID = bleUUID;

            currentBeaconsMap.put(deviceMAC, beaconLocation);
            for (BeaconHolder.IBeaconListener l : beaconListener) {
                l.onBeaconsAdded();
            }
        } else {
            currentBeaconsMap.get(result.getDevice().getAddress()).RSSI = result.getRssi();
            for (BeaconHolder.IBeaconListener l : beaconListener) {
                l.onBeaconChanged(currentBeaconsMap.get(result.getDevice().getAddress()));
            }
        }

    }

    public List<BeaconLocation> searchForBeacons(String text) {
        ArrayList<BeaconLocation> resultList = new ArrayList<>();
        text = text.toLowerCase();
        for (BeaconLocation b : beaconLocations) {
            if (b.placeId.toLowerCase().contains(text) || b.description.toLowerCase().contains(text) || b.roomId.toLowerCase().contains(text))
                resultList.add(b);
        }
        return resultList;
    }

    public void removeResult(ScanResult result) {
        BeaconLocation b = null;
        if (currentBeaconsMap.containsKey(result.getDevice().getAddress()))
            b = currentBeaconsMap.remove(result.getDevice().getAddress());
        for (BeaconHolder.IBeaconListener l : beaconListener) {
            l.onBeaconRemoved(b);
        }
    }


    public static interface IBeaconListener {

        void onBeaconChanged(BeaconLocation changedBeacon);

        void onBeaconsAdded();

        void onBeaconRemoved(BeaconLocation removedBeacon);
    }

    public String without(String mac) {
        return mac.replace(":", "");
    }

    public boolean compare(String a, String b) {
        return without(a).equals(without(b));
    }
}
