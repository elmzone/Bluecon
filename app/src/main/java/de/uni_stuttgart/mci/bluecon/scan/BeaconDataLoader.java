package de.uni_stuttgart.mci.bluecon.scan;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import de.uni_stuttgart.mci.bluecon.database.BeaconDBHelper;
import de.uni_stuttgart.mci.bluecon.database.DatabaseUtil;
import de.uni_stuttgart.mci.bluecon.domain.LocationInfo;

/**
 * Created by florian on 01.12.15.
 */
public class BeaconDataLoader extends AsyncTaskLoader<LocationInfo> {

    private LocationInfo entryData;
    private static final String TAG = "BlueconDataLoader";
    private BeaconDBHelper beaconDBHelper;
    private String macAddress;

    public BeaconDataLoader(Context context, BeaconDBHelper beaconDBHelper, String macaddress) {
        super(context);
        Log.d(TAG, "new data loader is created with macaddress " + macAddress);
        this.beaconDBHelper = beaconDBHelper;
        this.macAddress = macaddress;
    }


    @Override
    public LocationInfo loadInBackground() {
        String macAddressWithoutColon = macAddress.replace(":", "");
        LocationInfo location = DatabaseUtil.querySingleData(beaconDBHelper, macAddressWithoutColon, null);
        Log.d(TAG, "1: the callback of location query is " + macAddress + "::" + location);
        return location;
    }


    @Override
    protected void onStartLoading() {
        if (entryData != null) {
            deliverResult(entryData);
        } else {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(LocationInfo data) {
        if (data == null) {
            Log.d(TAG, "2: query information is null " + macAddress);
        } else {
            Log.d(TAG, "2: find the location info " + macAddress);
        }
        entryData = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(LocationInfo data) {
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        super.onReset();
    }
}
