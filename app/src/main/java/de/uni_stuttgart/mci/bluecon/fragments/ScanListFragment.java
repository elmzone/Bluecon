package de.uni_stuttgart.mci.bluecon.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import de.uni_stuttgart.mci.bluecon.BeaconHolder;
import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.act.MainActivity;
import de.uni_stuttgart.mci.bluecon.algorithm.CalcList;
import de.uni_stuttgart.mci.bluecon.bl.BlueconService;
import de.uni_stuttgart.mci.bluecon.bl.IBluetoothCallback;
import de.uni_stuttgart.mci.bluecon.domain.BeaconLocation;
import de.uni_stuttgart.mci.bluecon.ui.BeaconsAdapter;
import de.uni_stuttgart.mci.bluecon.ui.BeaconsViewHolder;
import de.uni_stuttgart.mci.bluecon.util2.ITtsProvider;
import de.uni_stuttgart.mci.bluecon.util2.SoundPoolPlayer;
import de.uni_stuttgart.mci.bluecon.util2.TtsWrapper;
import de.uni_stuttgart.mci.bluecon.util2.VibratorBuilder;

public class ScanListFragment
        extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, IBluetoothCallback, BeaconHolder.IBeaconListener, TtsWrapper.ITtsUser {

    //private static String TAG = "ScanListFragment";

    private RecyclerView mRecyclerView;
    private BeaconsAdapter mAdapter;
    //    private List<BeaconsInfo> resultList;
    private SwipeRefreshLayout swipeLayout;
    private boolean startButtonToggle = true;

    private String currentLocation = null;

    //Code Constants
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int DATA_CHECK_CODE = 2;
    private final static int REQUEST_SETTINGS = 3;
    private final static int BEACON_ADDED = 4;

    private final static long UPDATE_PERIOD = 3000;
    private final static String SPEAK_NAME = "name";
    private final static String TAG = "Bluecon";
    private final static String IS_TTS_ENABLED = "is TTS Enabled";

    private SoundPoolPlayer player;
    private VibratorBuilder vibrator;

    private Handler mHandler;
    //   private IBeacon beaconInterface;
    private SharedPreferences sharedPreferences;
    private CalcList calcList;


    private ITtsProvider tts;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) getActivity()).registerBlCallback(this);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.scan_fragment_main, container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.scan_recycler_view);
        registerForContextMenu(mRecyclerView);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        calcList = CalcList.getInstance();

        initRecyclerView(mRecyclerView);

        player = SoundPoolPlayer.getInstance(getActivity());
        vibrator = VibratorBuilder.getInstance(getActivity());

        swipeLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(true);
                //Handler is os.Handler not Java-Handler!!!
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            //Does nothing :)
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            // Enables Swiping only when at Top of the list
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean enable = false;
                if (mRecyclerView != null && mRecyclerView.getChildCount() > 0) {
                    enable = mRecyclerView.getChildAt(0).getTop() == 0;
                }
                swipeLayout.setEnabled(enable);
            }
        });


//        resultList = new ArrayList<BeaconsInfo>();

        mAdapter = new BeaconsAdapter(BeaconHolder.beacons());
        mRecyclerView.setAdapter(mAdapter);

        mHandler = new Handler();

        checkBLE(getActivity());

        checkBluetooth(getActivity());

        if (savedInstanceState != null) {
//            resultList = savedListInstance.getParcelableArrayList("list");
//            mAdapter.notifyDataSetChanged();
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_enableTTS:
//                if (item.isChecked()) {
////                    disableTTS();
//                    item.setChecked(false);
//                } else {
////                    enableTTS();
//                    item.setChecked(true);
//                }
//                sharedPreferences.edit().putBoolean(IS_TTS_ENABLED, item.isChecked()).apply();
//                return true;
//            case R.id.action_setting_activity:
//                Intent si = new Intent(getActivity(), SettingsActivity.class);
//                startActivityForResult(si, REQUEST_SETTINGS);
//                return true;
//            case R.id.action_register_beacon:
//                Intent ri = new Intent(getActivity(), RegisterBeacons.class);
//                startActivityForResult(ri, BEACON_ADDED);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.options, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO action
        return super.onContextItemSelected(item);
    }

    private void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        ((LinearLayoutManager) mLayoutManager).setOrientation(LinearLayoutManager.VERTICAL);
    }


    // ===================== Bluetooth ========================
    // ========================================================

    private void checkBLE(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        } else {
            Toast.makeText(context, R.string.ble_is_supported, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkBluetooth(Context context) {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Toast.makeText(context, R.string.bluetooth_is_supported, Toast.LENGTH_SHORT).show();
        }
    }

//
//    private void setStartButtonEnable(boolean isEnable) {
//        //TODO remove from code
//        // startServiceButton.setEnabled(isEnable);
//        //stopServiceButton.setEnabled(!isEnable);
//    }

    private void updateList() {
        Log.i(TAG, "get List" + BeaconHolder.beacons());
        @SuppressWarnings("unchecked")
        List<BeaconLocation> beaconsInfo = BeaconHolder.beacons();
//        List<BeaconsInfo> newList = calcList.calcList(beaconsInfo);
//
//        Collections.sort(newList);
////        resultList.clear();
//        if (!newList.isEmpty()) {
//            if (sharedPreferences.getBoolean("prefGuideSwitch", true)) {
//
//            }
//            if (sharedPreferences.getBoolean("prefVibrationSwitch", true)) {
//
//            }
//        }
//        resultList.addAll(newList);
        mAdapter.getBeaconsList().clear();
        mAdapter.getBeaconsList().addAll(beaconsInfo);
        mAdapter.notifyDataSetChanged();
    }

    Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            updateList();
//            mHandler.postDelayed(updateUI, UPDATE_PERIOD);
        }
    };

    private String getPreference() {
        StringBuilder builder = new StringBuilder();

        builder.append("\n Send report:").append(sharedPreferences.getBoolean("prefGuideSwitch", false));
        builder.append("\n Sync Threshold:").append(sharedPreferences.getString("prefThreshold", "default"));
        builder.append("\n Sync Frequency:").append(sharedPreferences.getString(getString(R.string.cnst_period), "default"));
        builder.append("\n Sync Link:").append(sharedPreferences.getString("prefLink", "http://meschup.hcilab.org/map"));
        return builder.toString();
    }


    public static boolean isRunning(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return pref.getBoolean(BlueconService.SERVICE_IS_RUNNING, false);
    }


    @Override
    public void onStart() {
        super.onStart();
//        if (mSpeech == null) {
//            if (!sharedPreferences.getBoolean(IS_TTS_ENABLED, false)) {
//                enableTTS();
//            }
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BeaconHolder.inst().registerBeaconListener(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        tts = TtsWrapper.inst().registerUser(this);
        Log.e(TAG, "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (BlueconService.isRunning) {
            mHandler.removeCallbacks(updateUI);
        }
//        TtsWrapper.inst().deregisterUser(this);
        BeaconHolder.inst().deregisterBeaconListener(this);
        ((MainActivity) getActivity()).deregisterBlCallback(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("prefThreshold")) {
            Log.i(TAG, "preference threshold changed!");
        }
        if (key.equals(getString(R.string.cnst_period))) {
            Log.i(TAG, "preference frequency changed!");
            if (BlueconService.isRunning) {
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent("period"));
            }
        }
        if (key.equals("prefAudio")) {
            Log.i(TAG, "preference AudioHint changed");
        }
    }

    @Override
    public void onBeaconChanged(BeaconLocation changedBeacon) {
        BeaconsViewHolder viewHolder = (BeaconsViewHolder) mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getBeaconsList().indexOf(changedBeacon));
        if (viewHolder != null)
            viewHolder.vRSSI_details.setText(String.valueOf(changedBeacon.RSSI));
//        mAdapter.notifyItemChanged(mAdapter.getBeaconsList().indexOf(changedBeacon));
    }

    @Override
    public void onBeaconsAdded() {
        updateList();
    }

    @Override
    public void onBeaconRemoved(BeaconLocation removedBeacon) {
        if (removedBeacon != null) {
            mAdapter.getBeaconsList().remove(removedBeacon);
            mAdapter.notifyItemRemoved(mAdapter.getBeaconsList().indexOf(removedBeacon));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBluetoothStarted() {

    }

    @Override
    public void onAdapterReady() {

    }
}
