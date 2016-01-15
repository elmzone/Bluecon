package de.uni_stuttgart.mci.bluecon.scan;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uni_stuttgart.mci.bluecon.BeaconsInfo;
import de.uni_stuttgart.mci.bluecon.BeaconsViewHolder;
import de.uni_stuttgart.mci.bluecon.BlueconService;
import de.uni_stuttgart.mci.bluecon.IBeacon;
import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.SettingsActivity;
import de.uni_stuttgart.mci.bluecon.Util.RecyclerItemClickListener;
import de.uni_stuttgart.mci.bluecon.Util.SoundPoolPlayer;
import de.uni_stuttgart.mci.bluecon.Util.VibratorBuilder;
import de.uni_stuttgart.mci.bluecon.algorithm.CalcList;
import de.uni_stuttgart.mci.bluecon.database.BeaconDBHelper;

public class ScanListFragment
        extends Fragment  implements SharedPreferences.OnSharedPreferenceChangeListener, BeaconsAdapter.OnListHeadChange
{

    //private static String TAG = "ScanListFragment";

    private RecyclerView mRecyclerView;
    private Adapter<BeaconsViewHolder> mAdapter;
    private List<BeaconsInfo> resultList;
    private SwipeRefreshLayout swipeLayout;
    private Button startServiceButton;
    private Button stopServiceButton;
    private boolean startButtonToggle = true;

    private List<BeaconsInfo> savedListInstance;
    private String currentLocation = null;

    //Code Constants
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int DATA_CHECK_CODE = 2;
    private final static int REQUEST_SETTINGS = 3;

    private TextToSpeech mSpeech;
    private final static long UPDATE_PERIOD = 3000;
    private final static String SPEAK_NAME = "name";
    private final static String TAG = "Bluecon";
    private final static String IS_TTS_ENABLED = "is TTS Enabled";

    private SoundPoolPlayer player;
    private VibratorBuilder vibrator;

    private Handler mHandler;
    private IBeacon beaconInterface;
    private SharedPreferences sharedPreferences;
    private CalcList calcList;

    private final static double EXPAND_RATIO = 2.8;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.scan_fragment_main, container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.scan_recycler_view);
        registerForContextMenu(mRecyclerView);
        startServiceButton = (Button) rootView.findViewById(R.id.service_start);
        stopServiceButton = (Button) rootView.findViewById(R.id.service_stop);
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

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    private int mOriginalHeight = 0;
                    private int mExpandHeight = 0;
                    private boolean isInited = false;

                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d(TAG, "now touched in View");
                        player.play(R.raw.expand);
                        LinearLayout expandArea = (LinearLayout) view.findViewById(R.id.expandArea);
                        if (!isInited) {
                            mOriginalHeight = view.getHeight();
                            mExpandHeight = (int) (mOriginalHeight * EXPAND_RATIO);
                            isInited = true;
                        }
                        ValueAnimator valueAnimator;
                        if (view.getHeight() == mOriginalHeight) {
                            readTheViewGroup(expandArea);
                            valueAnimator = ValueAnimator.ofInt(mOriginalHeight, mExpandHeight);
                            expandArea.setVisibility(View.VISIBLE);
                        } else {
                            valueAnimator = ValueAnimator.ofInt(mExpandHeight, mOriginalHeight);
                            expandArea.setVisibility(View.GONE);
                        }
                        valueAnimator.setDuration(200);
                        valueAnimator.setInterpolator(new LinearInterpolator());
                        final View theView = view;
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                theView.getLayoutParams().height = (int) animation.getAnimatedValue();
                                theView.requestLayout();
                            }
                        });
                        valueAnimator.start();
                    }

                })
        );

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startButtonToggle) {
                    Log.i(TAG, "Start Button clicked");
                    speakOut("begin scanning");
                    vibrator.vibrate(VibratorBuilder.LONG_LONG);
                    startServiceButton.setText(R.string.stop_service);
                    Intent intent = new Intent(getActivity(), BlueconService.class);
                    getActivity().bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
                    startButtonToggle = false;
                } else
                {
                    Log.i(TAG, "stop button clicked");
                    vibrator.vibrate(VibratorBuilder.SHORT_SHORT);
                    speakOut("stop scanning");
                    doUnbindService();
                    getActivity().unbindService(mServiceConnection);
                    startButtonToggle = true;
                }
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "stop button clicked");
                vibrator.vibrate(VibratorBuilder.SHORT_SHORT);
                speakOut("stop scanning");
                doUnbindService();
                getActivity().unbindService(mServiceConnection);
            }
        });

        resultList = new ArrayList<BeaconsInfo>();

        mAdapter = new BeaconsAdapter(resultList, this, BeaconDBHelper.getInstance(getActivity()));
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
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_enableTTS:
                if (item.isChecked()) {
                    disableTTS();
                    item.setChecked(false);
                } else {
                    enableTTS();
                    item.setChecked(true);
                }
                sharedPreferences.edit().putBoolean(IS_TTS_ENABLED, item.isChecked()).apply();
                return true;
            case R.id.action_setting_activity:
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                startActivityForResult(i, REQUEST_SETTINGS);
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void initRecyclerView (RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        ((LinearLayoutManager) mLayoutManager).setOrientation(LinearLayoutManager.VERTICAL);
    }

//============================= Helper Functions =======================================
    private void readTheViewGroup (ViewGroup viewGroup) {
        for (int i=0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                readTheViewGroup((ViewGroup) child);
            }
            else if (child instanceof TextView) {
                TextView textView = (TextView) child;
                speakOut(textView.getText().toString());
            }
        }

    }

    // ===================== Bluetooth ========================
    // ========================================================

    private void checkBLE(Context context) {
        if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
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


    // ==================== TextToSpeech ======================
    // ========================================================

    // TODO extract TTS to seperat class
    private void enableTTS() {
        if (mSpeech == null) {
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkTTSIntent, DATA_CHECK_CODE);
        }
    }

    private void disableTTS() {
        if (mSpeech != null) {
            mSpeech.stop();
            mSpeech.shutdown();
            mSpeech = null;
            Log.i(TAG, "TTS disabled");
        }
    }

    private void speakOut(String words) {
        if (Build.VERSION.SDK_INT < 21) {
            mSpeech.speak(words, TextToSpeech.QUEUE_ADD, null);
        }
        else {
            mSpeech.speak(words, TextToSpeech.QUEUE_ADD, null, SPEAK_NAME);
        }
    }

    private void setStartButtonEnable(boolean isEnable) {
        startServiceButton.setEnabled(isEnable);
        stopServiceButton.setEnabled(!isEnable);
    }

    private void updateList() {
        try {
            Log.i(TAG, "get List" + beaconInterface.getList());
            @SuppressWarnings("unchecked")
            List<BeaconsInfo> beaconsInfo = beaconInterface.getList();
            List<BeaconsInfo> newList = calcList.calcList(beaconsInfo);
            savedListInstance = newList;

            Collections.sort(newList);
            resultList.clear();
            if (!newList.isEmpty()) {
                if (sharedPreferences.getBoolean("prefGuideSwitch", true)) {

                }
                if (sharedPreferences.getBoolean("prefVibrationSwitch", true)) {

                }
            }
            resultList.addAll(newList);

            mAdapter.notifyDataSetChanged();
        }catch (RemoteException e) { e.printStackTrace(); }
    }

    @Override
    public void onLabelNameChange (String labelName, int position) {
        Log.i(TAG, "label name changed to" + labelName);
        String audioHint = sharedPreferences.getString("prefAudio", "");
        if (position == 0 && labelName != null) {
            if (currentLocation == null || !currentLocation.equals(labelName)) {
                currentLocation = labelName;

                if (audioHint.equals("")) {
                    speakOut(currentLocation);
                } else {
                    speakOut(audioHint + currentLocation);
                }
                if (sharedPreferences.getBoolean("prefGuideSwitch", true)) {
                    player.play(R.raw.new_direction);
                }
                if (sharedPreferences.getBoolean("prefVibrationSwitch", true)) {
                    vibrator.vibrate(VibratorBuilder.LONG_SHORT);
                }
            }
        }
    }

    Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            updateList();
            mHandler.postDelayed(updateUI, UPDATE_PERIOD);
        }
    };

    private String getPreference() {
        StringBuilder builder = new StringBuilder();

        builder.append("\n Send report:" + sharedPreferences.getBoolean("prefGuideSwitch", false));
        builder.append("\n Sync Threshold:" + sharedPreferences.getString("prefThreshold", "default"));
        builder.append("\n Sync Frequency:" + sharedPreferences.getString("prefFrequency", "default"));
        builder.append("\n Sync Link:" + sharedPreferences.getString("prefLink", "http://meschup.hcilab.org/map"));
        return builder.toString();
    }

    //=======================Service Function=======================
    //==============================================================

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service has connected");
            beaconInterface = IBeacon.Stub.asInterface(service);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doBindService();
                }
            }, 500);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "Service has disconnected");
            doUnbindService();
        }
    };

    private void doBindService() {
        // setStartButtonEnable(false);
        updateUI.run();
    }

    private void doUnbindService() {
        // setStartButtonEnable(true);
        beaconInterface = null;
        mHandler.removeCallbacks(updateUI);
        getActivity().unbindService(mServiceConnection);
    }

    public static boolean isRunning(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return pref.getBoolean(BlueconService.SERVICE_IS_RUNNING, false);
    }

    //===================== LifeCycle ===================
    //===================================================
    //LifeCycle: onCreate -> onStart -> onResume -> onPause -> onStop -> onDestroy

    @Override
    public void onStart() {
        if (mSpeech == null) {
            if (!sharedPreferences.getBoolean(IS_TTS_ENABLED,false)) {
                enableTTS();
            }
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        if (!isRunning(getActivity())) {
            setStartButtonEnable(true);
        } else {
            setStartButtonEnable(false);
            if (beaconInterface != null) {
                updateUI.run();
            } else {
                Intent intent = new Intent(getActivity(), BlueconService.class);
                getActivity().bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
                Toast.makeText(getActivity(), "service is still running, bind again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (beaconInterface != null) {
            mHandler.removeCallbacks(updateUI);
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        disableTTS();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

// ====================Intent Callback ======================
// ==========================================================
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getActivity(), R.string.ble_is_enabled, Toast.LENGTH_SHORT).show();
            } else  {
                Toast.makeText(getActivity(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
        if (requestCode == REQUEST_SETTINGS) {
            Toast.makeText(getActivity(), "change settings" + getPreference(), Toast.LENGTH_SHORT).show();
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "change finished with settings: " + getPreference());
            }
        }
        if (requestCode == DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        Log.d(TAG, "speech engine init");
                    }
                });
                mSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(SPEAK_NAME, "speech start");
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(SPEAK_NAME, "speech done");
                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.d(SPEAK_NAME, "speech error");
                    }
                });
            }
            else {
                Intent installTTSIntent = new Intent();
                Log.d(TAG, "speech engine install");
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("prefThreshold")) {
            Log.i(TAG, "preference threshold changed!");
        }
        if (key.equals("prefFrequency")) {
            Log.i(TAG, "preference frequency changed!");
            if (beaconInterface != null) {
                try {
                    beaconInterface.setPeriod();
                } catch (RemoteException e) {
                    Log.e(TAG, "error in set scanning period");
                    e.printStackTrace();
                }
            }
        }
        if (key.equals("prefAudio")) {
            Log.i(TAG, "preference AudioHint changed");
        }
    }

}
