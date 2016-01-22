package de.uni_stuttgart.mci.bluecon.scan;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_stuttgart.mci.bluecon.BeaconsInfo;
import de.uni_stuttgart.mci.bluecon.BeaconsViewHolder;
import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.algorithm.RangeThreshold;
import de.uni_stuttgart.mci.bluecon.database.BeaconDBHelper;
import de.uni_stuttgart.mci.bluecon.database.LocationInfo;
import de.uni_stuttgart.mci.bluecon.network.JSONLoader;

public class BeaconsAdapter extends Adapter<BeaconsViewHolder> {

    private List<BeaconsInfo> beaconsList;
    private static final String TAG = "BeaconsAdapter";
    private static final String NOT_FOUND = "not found";

    private Context context;
    Fragment contextFragment;
    BeaconDBHelper beaconDBHelper;
    private Map<String, BeaconsViewHolder> viewMap;
    int loaderID;
    JSONLoader jsonLoader;

    private int expandedPosition = -1;

    public interface OnListHeadChange {
        void onLabelNameChange(String labelname, int position);
    }

    OnListHeadChange mCallback;

    public BeaconsAdapter(List<BeaconsInfo> beaconsMap, Fragment fragment, de.uni_stuttgart.mci.bluecon.database.BeaconDBHelper beaconDBHelper) {
        this.beaconsList = beaconsMap;
        this.contextFragment = fragment;
        this.beaconDBHelper = beaconDBHelper;
        this.viewMap = new HashMap<>();
        loaderID = 0;
        mCallback = (OnListHeadChange) contextFragment;
        checkNetwork();
        jsonLoader = JSONLoader.getInstance(beaconDBHelper);
    }

    @Override
    public void onBindViewHolder(BeaconsViewHolder beaconsViewHolder, int position) {
        BeaconsInfo beaconsInfo = beaconsList.get(position);
        Log.d(TAG, "0: beaconsInfo is " + beaconsInfo);
        beaconsViewHolder.vName.setText(beaconsInfo.name);
        String rangeHint = readRssi(beaconsInfo.RSSI);
        beaconsViewHolder.vRSSI.setText(rangeHint);
        beaconsViewHolder.vRSSI_details.setText(String.valueOf(beaconsInfo.RSSI));

        Bundle bundle = new Bundle();
        bundle.putString("mac", beaconsInfo.macAddress);
        bundle.putInt("position", position);
        viewMap.put(beaconsInfo.macAddress, beaconsViewHolder);
        contextFragment.getLoaderManager().initLoader(loaderID, bundle, new BeaconDataLoaderCallbacks());
        loaderID++;

        if (position == expandedPosition) {
            beaconsViewHolder.vExpandArea.setVisibility(View.VISIBLE);
        } else {
            beaconsViewHolder.vExpandArea.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return beaconsList.size();
    }

    @Override
    public void onViewAttachedToWindow(BeaconsViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        long itemId = holder.getItemId();
        int itemPosition = holder.getPosition();

        Log.d("log", "id is " + itemId + "; position is " + itemPosition);
    }

    @Override
    public BeaconsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.beacon_layout, parent, false);
        return new BeaconsViewHolder(v);
    }

    private class BeaconDataLoaderCallbacks implements LoaderManager.LoaderCallbacks<LocationInfo> {
        private String mac;
        private int position;

        @Override
        public Loader<LocationInfo> onCreateLoader(int id, Bundle args) {
            this.mac = args.getString("mac");
            this.position = args.getInt("position");
            return new BeaconDataLoader(context, beaconDBHelper, mac);
        }

        @Override
        public void onLoadFinished(Loader<LocationInfo> loader, LocationInfo data) {
            BeaconsViewHolder mBeaconsViewHolder = viewMap.get(this.mac);
            if (data != null) {
                Log.d(TAG, "3: get location info from database " + data);
                if (data.label != null)
                    mBeaconsViewHolder.vName.setText(data.label);
                if (data.description != null)
                    mBeaconsViewHolder.vDescription.setText(data.description);
                if (data.category != null)
                    mBeaconsViewHolder.vCategory.setText(data.category);
                if (data.subcategory != null)
                    mBeaconsViewHolder.vSubcategory.setText(data.subcategory);

                Log.i(TAG, "interface is" + mCallback);
                mCallback.onLabelNameChange(data.label, position);
            } else {
                Log.d(TAG, "3: the data is null");
                mBeaconsViewHolder.vCategory.setText(NOT_FOUND);
                mBeaconsViewHolder.vSubcategory.setText(NOT_FOUND);
                mBeaconsViewHolder.vDescription.setText(NOT_FOUND);
                URL testURL = null;
                try {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(contextFragment.getActivity());
                    testURL = new URL(sharedPreferences.getString("prefLink", "http://meschup.hcilab.org/map"));
                    Log.i(TAG, "Database comes from " + testURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                jsonLoader.download(testURL, true, contextFragment.getActivity());
            }
        }

        @Override
        public void onLoaderReset(Loader<LocationInfo> loader) {

        }
    }

    private void checkNetwork() {
        ConnectivityManager connMgr = (ConnectivityManager) contextFragment.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(contextFragment.getActivity(), "Sorry, check network settings. No Network connected", Toast.LENGTH_SHORT).show();
        }
    }

    private String readRssi(int rssi) {
        rssi = Math.abs(rssi);
        String hint = "out of range";
        if (rssi < RangeThreshold.NEAR) {
            hint = "very close";
        } else if (rssi < RangeThreshold.MIDDLE) {
            hint = "near";
        } else if (rssi < RangeThreshold.FAR) {
            hint = "in range";
        }
        return hint;
    }
}
