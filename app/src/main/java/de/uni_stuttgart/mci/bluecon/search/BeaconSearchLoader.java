package de.uni_stuttgart.mci.bluecon.search;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import de.uni_stuttgart.mci.bluecon.database.BeaconDBHelper;
import de.uni_stuttgart.mci.bluecon.database.BeaconLocationTable;
import de.uni_stuttgart.mci.bluecon.database.DatabaseUtil;
import de.uni_stuttgart.mci.bluecon.domain.LocationInfo;

/**
 * Created by florian on 22.01.16.
 */
public class BeaconSearchLoader extends AsyncTaskLoader<ArrayList<LocationInfo>> {

    private ArrayList<LocationInfo> entryData;
    private BeaconDBHelper beaconDBHelper;
    private String keyword;
    private static final String TAG = "BeaconSearchLoader";

    public BeaconSearchLoader(Context context, BeaconDBHelper beaconDBHelper, String keyword) {
        super(context);
        Log.d(TAG, "new data loader is created with keyword " + keyword);
        this.beaconDBHelper = beaconDBHelper;
        this.keyword = keyword;
    }

    @Override
    public ArrayList<LocationInfo> loadInBackground() {
        ArrayList<LocationInfo> locations = DatabaseUtil.queryLikeData(beaconDBHelper, this.keyword, BeaconLocationTable.LocationEntry.COLUMN_NAME_LABEL, 100);
        Log.d(TAG, "1:the callback of keyword query is" + keyword + "::" + locations);
        return locations;
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
    public void deliverResult(ArrayList<LocationInfo> data) {
        if (data == null) {
            Log.d(TAG, "2:query keyword result is null" + keyword);
        } else {
            Log.d(TAG, "2:find the keyword info!" + keyword);
        }
        entryData = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<LocationInfo> data) {
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        // if we have a cursor, need to be closed.
        super.onReset();
    }
}
