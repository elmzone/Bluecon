package de.uni_stuttgart.mci.bluecon.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_stuttgart.mci.bluecon.domain.BeaconsInfo;
import de.uni_stuttgart.mci.bluecon.domain.RangeThreshold;

/**
 * Created by florian on 23.10.15.
 */
public class CalcList {

    private static CalcList instance = null;

    List<BeaconsInfo> outputList;
    Map<String, BeaconsInfo> beaconStorageMap;
    Map<String, int[]> map;
    Set<String> keySet;

    private CalcList() {
        map = new HashMap<>();
        beaconStorageMap = new HashMap<>();
    }

    public static CalcList getInstance(){
        if (instance == null)
            instance = new CalcList();
        return instance;
    }

    public List<BeaconsInfo> calcList(List<BeaconsInfo> inputList) {
        outputList = new ArrayList<>();
        keySet = new HashSet<>(map.keySet());

        for(BeaconsInfo beaconInfo : inputList){
            String macAddress = beaconInfo.macAddress;

            beaconStorageMap.put(macAddress, beaconInfo);
            int rssi = Math.abs(beaconInfo.RSSI);

            if (updateMap(macAddress, rssi)) {
                outputList.add(beaconInfo);
            }
        }
        calculateOthers(keySet);
        return outputList;
    }

    private boolean updateMap(String macAddress, int rssi) {
        int[] rssiArray = map.get(macAddress);
        int rssiReversed = RangeThreshold.FAR - rssi;
        if (rssiArray == null) {
            int[] initArray = new int[RangeThreshold.TOTAL];
            if(rssiReversed > 0) {
                initArray[0] = rssiReversed;
                for (int i = 1; i < RangeThreshold.TOTAL; i++) {
                    initArray[i] = -1;
                }
                map.put(macAddress, initArray);
                if(rssiReversed > RangeThreshold.THRESHOLD) {
                    return true;
                } else { return false;}
            } else {return false;}
        }else {
            popInArray(rssiReversed, rssiArray);
            keySet.remove(macAddress);
            return calculateArray(rssiArray, macAddress);
        }
    }

    private void popInArray(int neu, int[] array) {
        for (int i = 0; i < RangeThreshold.TOTAL; i++) {
            int temp = array[i];
            array[i] = neu;
            neu = temp;
        }
    }

    private boolean calculateArray(int[] array, String macAddress) {
        int sum = 0;
        for (int i = 0; i < RangeThreshold.TOTAL; i++) {
            sum = sum + array[i];
        }
        if (sum > RangeThreshold.THRESHOLD) {
            return true;
        } else {
            if (checkIsFull(array)) {
                map.remove(macAddress);
            }
            return false;
        }
    }

    private boolean checkIsFull(int[] array) {
        boolean isFull = true;
        for (int i = 0; i < RangeThreshold.TOTAL; i++) {
            if (array[i] < 0) {
                isFull = false;
                break;
            }
        }
        return isFull;
    }

    private void calculateOthers(Set<String> addressSet){
        for (String address : addressSet) {
            int[] rssiArray = map.get(address);
            popInArray(0, rssiArray);

            if (!calculateArray(rssiArray, address)) {
                map.remove(address);
                beaconStorageMap.remove(address);
            } else {
                BeaconsInfo reserved = beaconStorageMap.get(address);
                reserved.RSSI = calcAverage(rssiArray);
                outputList.add(beaconStorageMap.get(address));
            }
        }
    }

    private int calcAverage (int[] array) {
        int sum = 0;
        for (int i=0; i<RangeThreshold.TOTAL; i++) {
            sum = sum + array[i];
        }
        return sum/RangeThreshold.TOTAL;
    }
}
