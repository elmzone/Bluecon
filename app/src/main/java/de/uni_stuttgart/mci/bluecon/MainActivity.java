package de.uni_stuttgart.mci.bluecon;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
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
import com.google.api.services.proximitybeacon.v1beta1.Proximitybeacon;
import com.google.api.services.proximitybeacon.v1beta1.ProximitybeaconRequestInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.uni_stuttgart.mci.bluecon.fragments.NavigationListFragment;
import de.uni_stuttgart.mci.bluecon.util.BlueconPageAdapter;
import de.uni_stuttgart.mci.bluecon.util.TtsWrapper;

// API-OAuth:  739731480344-19rs3rqn9ncp4ebk035vph1fm9utgard.apps.googleusercontent.com

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, BeaconHolder.BeaconListener {
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


    private Handler handler;
    private final int duration = 3; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 440; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public void registerBlCallback(IBluetoothCallback bluetoothCallback) {
        blCallbacks.add(bluetoothCallback);
    }

    public void deregisterBlCallback(IBluetoothCallback bluetoothCallback) {
        blCallbacks.remove(bluetoothCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final ActionBar actionBar = getActionBar();

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
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(pageAdapter);
        viewPager.setCurrentItem(0);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
//        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);

        //sets which lever controls the Audio Output
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //Keeps Screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        gApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        gApiClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://de.uni_stuttgart.mci.bluecon/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
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

    private void playExampleBeep(@NonNull final double freqOfTone, @NonNull final int duration) {
        handler = new Handler();
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < numSamples; ++i) {
                    sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
                }

                // convert to 16 bit pcm sound array
                // assumes the sample buffer is normalised.
                int idx = 0;
                for (final double dVal : sample) {
                    // scale to maximum amplitude
                    final short val = (short) ((dVal * 32767));
                    // in 16 bit wav PCM, first byte is the low order byte
                    generatedSnd[idx++] = (byte) (val & 0x00ff);
                    generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

                }
                handler.post(new Runnable() {

                    public void run() {
                        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                                AudioTrack.MODE_STATIC);
                        audioTrack.write(generatedSnd, 0, generatedSnd.length);
                        audioTrack.play();
                    }
                });
            }
        });
        thread.start();


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
        BeaconHolder.inst().registerBeaconListener(this);
    }

    private void stopBlService() {
        stopService(new Intent(this, BlueconService.class));
        BeaconHolder.inst().deregisterBeaconListener(this);
    }

//    private void startBlService() {
//        Intent intent = new Intent(this, BlueconService.class);
//        //   getActivity().bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
//        startService(intent);
//    }
//
//    private void stopBlService() {
//        stopService(new Intent(this, BlueconService.class));
//    }

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


        PublishOptions optionsP = new PublishOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "no longer publishing");
//                            updateSharedPreference(Constants.KEY_PUBLICATION_TASK,
//                                    Constants.TASK_NONE);
                    }
                }).build();

        String id = InstanceID.getInstance(getApplicationContext()).getId();
        Message msg = new Message(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8});

        Nearby.Messages.publish(gApiClient, msg, optionsP)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "published successfully");
                        } else {
                            Log.i(TAG, "could not publish");
//                                handleUnsuccessfulNearbyResult(status);
                        }
                    }
                });
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
        builder.append("\n Sync Link:").append(sharedPreferences.getString(getString(R.string.prefs_link_url), "http://meschup.hcilab.org/map"));
        return builder.toString();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://de.uni_stuttgart.mci.bluecon/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        if (gApiClient.isConnected() && !isChangingConfigurations()) {
            gApiClient.disconnect();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }


    @Override
    public void onBeaconsChanged(List<BeaconsInfo> changedBeacons) {
//        updateList();
    }

    @Override
    public void onBeaconsAdded() {

//        updateList();
    }

    @Override
    public void onBeaconsRemoved(List<BeaconsInfo> removedBeacons) {

//        updateList();
    }


}
