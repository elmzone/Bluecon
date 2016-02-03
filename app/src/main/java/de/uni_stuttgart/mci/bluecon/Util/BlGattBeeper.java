package de.uni_stuttgart.mci.bluecon.Util;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.uni_stuttgart.mci.bluecon.BlueconService;

/**
 * Created by flori_000 on 03.02.2016.
 */
public class BlGattBeeper extends BluetoothGattCallback {
    private static String TAG = "BlGattBeeper ";

    BluetoothGatt gattServer;

    public void beepFor (TimeUnit timeUnit) {
//        gattServer =
    }


    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
            gattServer = gatt;
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            gatt.close();
            gattServer = null;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            gattServer = gatt;
            for (BluetoothGattService s : gatt.getServices()) {
                Log.i(TAG, "onServicesDiscovered: " + s.getUuid().toString());
            }
            BluetoothGattService s = gatt.getService(UUID.fromString("31300000-5347-4233-3074-656764696c42"));
            if (s != null) {
                for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                    Log.i(TAG, "onServicesDiscovered: Characteristics: " + c.getUuid().toString());
                    if (c.getUuid().equals(UUID.fromString("31300101-5347-4233-3074-656764696c42"))) {
                        gatt.readCharacteristic(c);
                    }
                }
//                    BluetoothGattCharacteristic weatherC = s.getCharacteristic(UUID.fromString(Constants.GATT_WEATHER_TODAY));
//                    gatt.readCharacteristic(weatherC);21-01-04-E803-0000-0000-0000
//                } else {
//                    s = gatt.getService(UUID.fromString(Constants.GATT_SERVICE_PUB_TRANSP));
//                    BluetoothGattCharacteristic transpC = s.getCharacteristic(UUID.fromString(Constants.GATT_PUB_TRANSP_BUS));
//                    gatt.readCharacteristic(transpC);
            }
        } else {
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (characteristic.getValue() != null) {
                if (characteristic.getUuid().equals(UUID.fromString("31300101-5347-4233-3074-656764696c42"))) {
                    byte[] pinChara = characteristic.getValue();
                    pinChara[2] = 0x24;
                    characteristic.setValue(pinChara);
                    gatt.writeCharacteristic(characteristic);
                } else if (characteristic.getUuid().equals(UUID.fromString("31300102-5347-4233-3074-656764696c42"))) {

                }
            }

        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        gattServer = gatt;
        if (characteristic.getUuid().equals(UUID.fromString("31300101-5347-4233-3074-656764696c42"))) {
            BluetoothGattService s = gatt.getService(UUID.fromString("31300000-5347-4233-3074-656764696c42"));
            if (s != null) {
                BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString("31300102-5347-4233-3074-656764696c42"));
                c.setValue(new byte[]{0x21, 01, 02, (byte) 0xB4, 46, (byte) 0xF4, 01, (byte) 0xF4, 01, (byte) 0xF4, 01});
                if (gatt.writeCharacteristic(c)) {
                    Log.i(TAG, "onServicesDiscovered: Command Characteristic");
                }
            }
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        gattServer = gatt;
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "onCharacteristicChanged: ");
        gattServer = gatt;
    }
}
