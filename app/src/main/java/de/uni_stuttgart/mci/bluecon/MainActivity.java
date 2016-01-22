package de.uni_stuttgart.mci.bluecon;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;

import de.uni_stuttgart.mci.bluecon.Util.BlueconPageAdapter;

// API-OAuth:  739731480344-8nq2u5s9psn47gqn7u4f8e2eer1gi9on.apps.googleusercontent.com

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static String TAG = "main Activity";
    FragmentManager fragmentManager;
    BlueconPageAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected to Nearby-API");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (RESULT_OK == resultCode) {

                }
                break;
        }
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

    public static interface IBluetoothCallback {

        void onBluetoothStarted();

        void onAdapterReady();
    }

    private class BrdcstRcvr {

    }
}
