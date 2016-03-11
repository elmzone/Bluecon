package de.uni_stuttgart.mci.bluecon.bl;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.uni_stuttgart.mci.bluecon.BeaconHolder;
import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.act.MainActivity;
import de.uni_stuttgart.mci.bluecon.domain.BeaconsInfo;

public class BlueconService extends Service {

    protected static final long LONG_SCAN_PERIOD = 8000;
    protected static final long SHORT_SCAN_PERIOD = 2000;
    protected static final long MIDDLE_SCAN_PERIOD = 5000;

    public static boolean isRunning = false;

    public static String SERVICE_IS_RUNNING = "BlueconService.blueconScanService";
    protected boolean mScanning = false;
    private static String TAG = "bluecon service";

//    private Handler scanHandler;
    private Map<String, BeaconsInfo> resultsMap;

    //config variable
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanSettings scanSettings;
    private ScanListener scanCallback;
    private NotificationManager mNotificationManager;

    private SharedPreferences preferences;
//    private long scanPeriod;

    private BrdcstRcvr brdcst;

//    private final IBeacon.Stub mBinder = new IBeacon.Stub() {
//
//        public int getCount() {
//            return count;
//        }
//
//
//        public String getName() {
//            return null;
//        }
//
//        @Override
//        public List<BeaconsInfo> getList() throws RemoteException {
//            return resultList;
//        }
//
//
//        public void setPeriod() {
//            setScanPeriod();
//        }
//    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "a client is bound to service");
        return null;
    }

//    @Override
//    public boolean onUnbind(Intent intent) {
//        Log.d(TAG, "a client is unbound");
//        return super.onUnbind(intent);
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;

        brdcst = new BrdcstRcvr();

        IntentFilter filter = new IntentFilter();
        filter.addAction("period");
        filter.addAction(getString(R.string.intent_gatt_open));
        LocalBroadcastManager.getInstance(this).registerReceiver(brdcst, filter);

//        scanHandler = new Handler();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
//        setScanPeriod();
        setRunning(true);

        final BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.i(TAG, "Bluetooth not enabled");
        }
        resultsMap = new HashMap<>();
//        BeaconHolder.inst().setCurrentBeacons(new ArrayList<BeaconsInfo>());
        initScanCallback();
//        scanRunnable.run();

        scanBLE(true);

        createNotification();
    }

//    public void setScanPeriod() {
//        String scanFrequecy = preferences.getString(getString(R.string.cnst_period), "1");
//        assert scanFrequecy != null;
//        int frequency = Integer.parseInt(scanFrequecy);
//        switch (frequency) {
//            case 0:
//                scanPeriod = LONG_SCAN_PERIOD;
//                break;
//            case 1:
//                scanPeriod = MIDDLE_SCAN_PERIOD;
//                break;
//            case 2:
//                scanPeriod = SHORT_SCAN_PERIOD;
//                break;
//            default:
//                scanPeriod = SHORT_SCAN_PERIOD;
//        }
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "service started");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
//        scanHandler.removeCallbacks(scanRunnable);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(brdcst);

        setRunning(false);
        Log.e(TAG, "service is destroyed");

        destroyNotification();
        scanBLE(false);
    }

//    Runnable scanRunnable = new Runnable() {
//        @Override
//        public void run() {
//            scanBLE(true);
//            scanHandler.postDelayed(scanRunnable, scanPeriod);
//        }
//    };

    public void scanBLE(boolean enable) {
        if (Build.VERSION.SDK_INT >= 21) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            //more setting please check new api
            scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
        }

        if (enable) {
            mScanning = true;
            resultsMap.clear();
            startScan();
//			Stops scanning after a pre-defined scan period.
//            scanHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    Log.d(TAG, "now scan will stop");
//                    stopScan();
//                    mapToList(resultsMap, BeaconHolder.beacons());
//                    Log.d(TAG, "now the list is" + BeaconHolder.beacons());
//                }
//            }, scanPeriod);
        } else {
            if (mScanning) {
                mScanning = false;
                stopScan();
            }
        }
        Log.d(TAG, "now the scanning state is" + mScanning);

    }

    private void setRunning(boolean running) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(SERVICE_IS_RUNNING, running);
        editor.apply();
    }

    // ====================Anti-Fragmentation========================
    // ==============================================================

    @SuppressWarnings("deprecation")
    private void startScan() {
        final UUID[] uuids = new UUID[]{UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")};

        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan(uuids, (BluetoothAdapter.LeScanCallback) scanCallback);
//            mBluetoothAdapter.startLeScan((BluetoothAdapter.LeScanCallback) scanCallback);
        } else {
            mBluetoothLeScanner.startScan(new ArrayList<ScanFilter>() {
                {
                    add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(uuids[0])).build());
                }
            }, scanSettings,  scanCallback);
//            mBluetoothLeScanner.startScan(null, scanSettings, scanCallback);
//            mBluetoothLeScanner.flushPendingScanResults( scanCallback);
        }
    }

    @SuppressWarnings("deprecation")
    private void stopScan() {
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.stopLeScan((BluetoothAdapter.LeScanCallback) scanCallback);
        } else {
            mBluetoothLeScanner.flushPendingScanResults(scanCallback);
            mBluetoothLeScanner.stopScan(scanCallback);
        }
    }


    private void initScanCallback() {
        // Device scan callback in API 21.
        if (Build.VERSION.SDK_INT >= 21) {
            scanCallback = new ScanListener();

        }
    }

//    private void addBeaconToMap(ScanResult result, Map<String, BeaconsInfo> map) {
//        int receiveRSSI = result.getRssi();
//        BluetoothDevice receiveBeacon = result.getDevice();
//
//        String deviceMAC = receiveBeacon.getAddress();
//        Log.d(TAG, "macAddress from API 21 is" + deviceMAC);
//        if (!map.containsValue(deviceMAC)) {
//            ScanRecord receiveRecord = result.getScanRecord();
//
//            String deviceName = "NULL NAME";
//            String mServiceName = receiveBeacon.getName();
//            if (mServiceName != null) {
//                deviceName = mServiceName;
//            }
//            List<ParcelUuid> mServiceUUID = receiveRecord.getServiceUuids();
//            String bleUUID = "NULL UUID";
//            if (mServiceUUID != null) {
//                bleUUID = receiveRecord.getServiceUuids().toString();
//            }
//            Log.d("recordInfo", receiveRecord.toString());
//
//            BeaconsInfo beaconsInfo = new BeaconsInfo();
//            beaconsInfo.name = deviceName;
//            beaconsInfo.RSSI = receiveRSSI;
//            beaconsInfo.macAddress = deviceMAC;
//            beaconsInfo.UUID = bleUUID;
//
//            map.put(deviceMAC, beaconsInfo);
//        }
//    }

//    private void addBeaconToMap(BluetoothDevice device, int rssi, byte[] scanRecord, Map<String, BeaconsInfo> map) {
//        BeaconsInfo beaconInfo = new BeaconsInfo();
//        beaconInfo.RSSI = rssi;
//        beaconInfo.macAddress = device.getAddress();
//
//        String recordInfo = new String(scanRecord);
//        String recordUUID = "NULL UUID";
//        if (!recordInfo.equals("")) {
//            Log.d(TAG, "the scan record string is" + recordInfo);
//            recordUUID = recordInfo;
//        }
//        beaconInfo.UUID = recordUUID;
//
//        String deviceName = "NULL NAME";
//        String mServiceName = device.getName();
//        if (mServiceName != null) {
//            deviceName = mServiceName;
//        }
//        beaconInfo.name = deviceName;
//        map.put(beaconInfo.macAddress, beaconInfo);
//    }

    // ==================Notification===================
    // =================================================
    public void createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Bluecon")
                        .setContentText("Service Created");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(R.id.nid_bl_conn, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);


        Intent brdcstIntent = new Intent(this, BrdcstStop.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, R.id.nid_bl_conn, brdcstIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.addAction(R.drawable.ic_launcher, "Stop Bluetooth", pendingIntent);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(R.id.nid_bl_conn, mBuilder.build());
    }

    private void destroyNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(R.id.nid_bl_conn);
    }

//    private void updateNotification() {
//        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//
//
//        builder
//                .setContentTitle("Bluecon")
//                .setContentText("Continue moving")
//                .setSmallIcon(R.drawable.ic_launcher)
//        ;
//
//        Intent brdcstIntent = new Intent(this, BrdcstStop.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, R.id.nid_bl_conn, brdcstIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        builder.addAction(R.drawable.ic_launcher, "Stop Bluetooth", pendingIntent);
//
//        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(R.id.nid_bl_conn, builder.build());
//    }

    private class ScanListener extends ScanCallback {

        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
            if (callbackType == ScanSettings.CALLBACK_TYPE_MATCH_LOST)
                BeaconHolder.inst().removeResult(result);
            else
                BeaconHolder.inst().handleNewBeacon(result);
// if (resultsMap.containsKey(result.getDevice().getAddress())) {
//
//            } else {
//                addBeaconToMap(result, resultsMap);
//                BeaconHolder.inst().addBeacons(mapToList(resultsMap));
//            }
        }

        public void onBatchScanResults(java.util.List<android.bluetooth.le.ScanResult> results) {
            Log.d(TAG, "event listener is called!!!!");
            Log.d(TAG, "batch result are:" + results);
            //mAdapter.notifyDataSetChanged();
            for (int i = 0; i < results.size(); i++) {
                ScanResult result = results.get(i);

                Log.d(TAG, "add item" + result + "to list");
                BeaconHolder.inst().handleNewBeacon(result);
                }

        }

        public void onScanFailed(int errorCode) {
            Log.d(TAG, "scan error code is:" + errorCode);
        }
    }

    private class BrdcstRcvr extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(getString(R.string.intent_gatt_open))) {
                String mac = intent.getStringExtra(getString(R.string.bndl_mac));
                BlGattBeeper beeper = new BlGattBeeper();
                mBluetoothAdapter.getRemoteDevice(mac).connectGatt(context, false, beeper);
//                beeper.beepFor(2, TimeUnit.SECONDS);
            }
//            setScanPeriod();
        }
    }

}
