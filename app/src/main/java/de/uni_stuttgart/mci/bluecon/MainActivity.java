package de.uni_stuttgart.mci.bluecon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.uni_stuttgart.mci.bluecon.Util.BlueconPageAdapter;
import de.uni_stuttgart.mci.bluecon.Util.TtsWrapper;

// API-OAuth:  739731480344-8nq2u5s9psn47gqn7u4f8e2eer1gi9on.apps.googleusercontent.com

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static String TAG = "main Activity";

    //Code Constants
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int DATA_CHECK_CODE = 2;
    private final static int REQUEST_SETTINGS = 3;


    static final int TTL_IN_SECONDS = 3 * 60;

    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS).build();

    private final static String SPEAK_NAME = "name";
    FragmentManager fragmentManager;
    BlueconPageAdapter pageAdapter;
    private SharedPreferences sharedPreferences;

    private List<IBluetoothCallback> blCallbacks = new ArrayList<>();
    private GoogleApiClient gApiClient;

    public void registerBlCallback(IBluetoothCallback bluetoothCallback) {
        blCallbacks.add(bluetoothCallback);
    }

    public void deregisterBlCallback(IBluetoothCallback bluetoothCallback) {
        blCallbacks.remove(bluetoothCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!TtsWrapper.exists()) {
            TtsWrapper.init(BlueconApp.inst());
        }
        //FragmentManager initialized to manage the 2 Scan and Search Views
        fragmentManager = getSupportFragmentManager();
        pageAdapter = new BlueconPageAdapter(fragmentManager);

        //the ViewPager puts the Fragments in the activity
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pageAdapter);
        viewPager.setCurrentItem(0);

        //sets which lever controls the Audio Output
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //Keeps Screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();
        gApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        gApiClient.connect();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Runnable focus = new Runnable() {
            @Override
            public void run() {
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) currentFocus.clearFocus();
                findViewById(R.id.service_start).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }
        };
        Executors.newSingleThreadScheduledExecutor().schedule(focus, 1, TimeUnit.SECONDS);
    }

    private void checkBLE(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
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

    // ====================Intent Callback ======================
// ==========================================================
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_ENABLE_BT) {
//            if (resultCode == Activity.RESULT_OK) {
//                Toast.makeText(this, R.string.ble_is_enabled, Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//        if (requestCode == REQUEST_SETTINGS) {
//            Toast.makeText(this, "change settings" + getPreference(), Toast.LENGTH_SHORT).show();
//            if (resultCode == Activity.RESULT_OK) {
//                Log.d(TAG, "change finished with settings: " + getPreference());
//            }
//        }
//        //TODO check TTS Data
//        if (requestCode == DATA_CHECK_CODE) {
//        }
//    }

    private void startBlService() {
        Intent intent = new Intent(this, BlueconService.class);
        //   getActivity().bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
        startService(intent);
    }

    private void stopBlService() {
        stopService(new Intent(this, BlueconService.class));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected to Nearby-API");
        if (!gApiClient.isConnected()) {
            if (!gApiClient.isConnecting()) {
                gApiClient.connect();
            }
        } else {
            subscribe();
        }
    }


    private void subscribe() {
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "no longer subscribing");
//                            updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK,
//                                    Constants.TASK_NONE);
                    }
                }).build();

        Nearby.Messages.subscribe(gApiClient, new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.i(TAG, "found " + new String(message.getContent()));
            }
        }, options)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "subscribed successfully");
                        } else {
                            Log.i(TAG, "could not subscribe");
                            try {
                                status.startResolutionForResult(MainActivity.this,
                                        1001);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
//                                handleUnsuccessfulNearbyResult(status);
                        }
                    }
                });


//            PublishOptions options = new PublishOptions.Builder()
//                    .setStrategy(PUB_SUB_STRATEGY)
//                    .setCallback(new PublishCallback() {
//                        @Override
//                        public void onExpired() {
//                            super.onExpired();
//                            Log.i(TAG, "no longer publishing");
////                            updateSharedPreference(Constants.KEY_PUBLICATION_TASK,
////                                    Constants.TASK_NONE);
//                        }
//                    }).build();
//
//            String id = InstanceID.getInstance(getApplicationContext()).getId();
//            Message msg = new Message(new byte[]{0, 1, 2, 3, 4, 5, 6});
//
//            Nearby.Messages.publish(gApiClient, msg, options)
//                    .setResultCallback(new ResultCallback<Status>() {
//
//                        @Override
//                        public void onResult(Status status) {
//                            if (status.isSuccess()) {
//                                Log.i(TAG, "published successfully");
//                            } else {
//                                Log.i(TAG, "could not publish");
////                                handleUnsuccessfulNearbyResult(status);
//                            }
//                        }
//                    });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            // User was presented with the Nearby opt-in dialog and pressed "Allow".
            if (resultCode == Activity.RESULT_OK) {
                // We track the pending subscription and publication tasks in MainFragment. Once
                // a user gives consent to use Nearby, we execute those tasks.
                subscribe();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User was presented with the Nearby opt-in dialog and pressed "Deny". We cannot
                // proceed with any pending subscription and publication tasks. Reset state.
            } else {
                Toast.makeText(this, "Failed to resolve error with code " + resultCode,
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

        Log.d(TAG, "Connection suspended to Nearby-API");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed to Nearby-API");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private String getPreference() {
        StringBuilder builder = new StringBuilder();

        builder.append("\n Send report:").append(sharedPreferences.getBoolean("prefGuideSwitch", false));
        builder.append("\n Sync Threshold:").append(sharedPreferences.getString("prefThreshold", "default"));
        builder.append("\n Sync Frequency:").append(sharedPreferences.getString(getString(R.string.cnst_period), "default"));
        builder.append("\n Sync Link:").append(sharedPreferences.getString("prefLink", "http://meschup.hcilab.org/map"));
        return builder.toString();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gApiClient.isConnected() && !isChangingConfigurations()) {
            gApiClient.disconnect();
        }
    }

    private class BrdcstRcvr {

    }
}
