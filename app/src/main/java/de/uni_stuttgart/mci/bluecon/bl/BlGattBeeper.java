package de.uni_stuttgart.mci.bluecon.bl;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.uni_stuttgart.mci.bluecon.BlueconApp;
import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.util.ScheduleUtil;

/**
 * Created by flori_000 on 03.02.2016.
 */
public class BlGattBeeper extends BluetoothGattCallback {
    private static String TAG = "BlGattBeeper ";

    BluetoothGatt gattServer;
    private Beeping b;


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
        if (characteristic.getUuid().equals(UUID.fromString("31300102-5347-4233-3074-656764696c42"))) {
            switch (b.statusBeeper) {
                case BEEP_SENT:
                    b.beepFor();
                    break;
                case BEEP_STOP_SENT:
                    b.closeGatt();
            }
        } else if (characteristic.getUuid().equals(UUID.fromString("31300101-5347-4233-3074-656764696c42"))) {

            b = new Beeping().sendBeep();
//            BluetoothGattService s = gatt.getService(UUID.fromString("31300000-5347-4233-3074-656764696c42"));
//            if (s != null) {
//                BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString("31300102-5347-4233-3074-656764696c42"));
//                c.setValue(new byte[]{0x21, 01, 02, (byte) 0xB4, 46, (byte) 0xF4, 01, (byte) 0xF4, 01, (byte) 0xF4, 01});
//                if (gatt.writeCharacteristic(c)) {
//                    Log.i(TAG, "onServicesDiscovered: Command Characteristic");
//                }
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

    private class Beeping {
        public StatusBeeper statusBeeper;

        public Beeping sendBeep() {
            BluetoothGattService s = gattServer.getService(UUID.fromString("31300000-5347-4233-3074-656764696c42"));
            BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString("31300102-5347-4233-3074-656764696c42"));
            Objects.requireNonNull(c);

            SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(BlueconApp.inst());
            int dura = Integer.parseInt(pm.getString(BlueconApp.inst().getString(R.string.pref_beep_dura_key), "2"));
            int frequ = Integer.parseInt(pm.getString(BlueconApp.inst().getString(R.string.pref_beep_freq_key), "880"));
            int freq = (16000000 / (frequ * 2));
            int first = freq >> 8;
            int second = freq - first;
            c.setValue(new byte[]{0x21, 01, 01, (byte) second, (byte) first, (byte) 0xF4, 01, (byte) 0xF4, 01, (byte) 0xF4, 01});

            statusBeeper = StatusBeeper.BEEP_SENT;
            if (gattServer.writeCharacteristic(c)) {
                Log.i(TAG, "onServicesDiscovered: Command Characteristic");
            }
            return this;
        }

        public void beepFor() {

            SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(BlueconApp.inst());
            int dura = Integer.parseInt(pm.getString(BlueconApp.inst().getString(R.string.pref_beep_dura_key), "2"));
            BluetoothGattService s = gattServer.getService(UUID.fromString("31300000-5347-4233-3074-656764696c42"));
            BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString("31300102-5347-4233-3074-656764696c42"));
            Objects.requireNonNull(c);
            ScheduleUtil.scheduleWork(new Runnable() {
                public Runnable init(BluetoothGattCharacteristic c) {
                    this.c = c;
                    return this;
                }

                public BluetoothGattCharacteristic c;


                @Override
                public void run() {

                    this.c.setValue(new byte[]{0x21, 01, 03, (byte) 0xB4, 46, 00, 00, 00, 00, 00, 00});
                    statusBeeper = StatusBeeper.BEEP_STOP_SENT;
                    if (gattServer.writeCharacteristic(c)) {
                        Log.i(TAG, "onServicesDiscovered: Command Characteristic");
                    }
                }
            }.init(c), dura, TimeUnit.SECONDS);


        }

        public void closeGatt() {
            ScheduleUtil.scheduleWork(new Runnable() {


                @Override
                public void run() {

                    gattServer.close();
//                    gattServer.disconnect();
                }
            }, 100, TimeUnit.MILLISECONDS);
        }
    }

    enum StatusBeeper {
        BEEP_SENT, BEEP_STOP_SENT
    }
}
