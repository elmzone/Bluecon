package de.uni_stuttgart.mci.bluecon.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.domain.BeaconLocation;
import de.uni_stuttgart.mci.bluecon.domain.RangeThreshold;
import de.uni_stuttgart.mci.bluecon.util.SoundPoolPlayer;

public class BeaconsAdapter extends Adapter<BeaconsViewHolder> {

    private List<BeaconLocation> beaconsList;
    private static final String TAG = "BeaconsAdapter";

    private Context context;


    public List<BeaconLocation> getBeaconsList() {
        return beaconsList;
    }

    public BeaconsAdapter(List<BeaconLocation> beaconsMap) {
        this.beaconsList = beaconsMap;
//        this.beaconDBHelper = beaconDBHelper;
//        jsonLoader = JSONLoader.getInstance(beaconDBHelper);
    }

    @Override
    public BeaconsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.beacon_layout_with_buttons, parent, false);
        BeaconsViewHolder vh = new BeaconsViewHolder(v);
        ActionExpand a = new ActionExpand();
        vh.parent.setOnClickListener(a);
        a.setVh(vh);
        vh.parent.setTag(a);

        return vh;
    }

    @Override
    public void onBindViewHolder(BeaconsViewHolder vh, int position) {
        final BeaconLocation beaconLocation = beaconsList.get(position);
        Log.d(TAG, "0: beaconLocation is " + beaconLocation);

        vh.vName.setText(beaconLocation.placeId);
        String rangeHint = readRssi(beaconLocation.RSSI);
        vh.vRSSI.setText(rangeHint);
        vh.vRSSI_details.setText(String.valueOf(beaconLocation.RSSI));
        vh.vPlaceId.setText(beaconLocation.type);
        vh.vRoomId.setText(beaconLocation.roomId);
        vh.vDescription.setText(beaconLocation.description);

        vh.btnBeep.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(new Intent(v.getContext().getString(R.string.intent_gatt_open)).putExtra(v.getContext().getString(R.string.bndl_mac), beaconLocation.macAddress));

                                          }
                                      }
        );


        ActionExpand a = (ActionExpand) vh.parent.getTag();
        a.setVh(vh);
//        Bundle bundle = new Bundle();
//        bundle.putString("mac", beaconLocation.macAddress);
//        bundle.putInt("position", position);
//        viewMap.put(beaconLocation.macAddress, beaconsViewHolder);
//        contextFragment.getLoaderManager().initLoader(loaderID, bundle, new BeaconDataLoaderCallbacks());
//        loaderID++;
    }

    @Override
    public int getItemCount() {
        return beaconsList.size();
    }

//    private class BeaconDataLoaderCallbacks implements LoaderManager.LoaderCallbacks<LocationInfo> {
//        private String mac;
//        private int position;
//
//        @Override
//        public Loader<LocationInfo> onCreateLoader(int id, Bundle args) {
//            this.mac = args.getString("mac");
//            this.position = args.getInt("position");
//            return new BeaconDataLoader(context, beaconDBHelper, mac);
//        }
//
//        @Override
//        public void onLoadFinished(Loader<LocationInfo> loader, LocationInfo data) {
//            BeaconsViewHolder mBeaconsViewHolder = viewMap.get(this.mac);
//            if (data != null) {
//                Log.d(TAG, "3: get location info from database " + data);
//                if (data.label != null)
//                    mBeaconsViewHolder.vName.setText(data.label);
//                if (data.description != null)
//                    mBeaconsViewHolder.vDescription.setText(data.description);
//                if (data.category != null)
//                    mBeaconsViewHolder.vRoomId.setText(data.category);
//                if (data.subcategory != null)
//                    mBeaconsViewHolder.vPlaceId.setText(data.subcategory);
//
//            } else {
//                Log.d(TAG, "3: the data is null");
//                mBeaconsViewHolder.vRoomId.setText(NOT_FOUND);
//                mBeaconsViewHolder.vPlaceId.setText(NOT_FOUND);
//                mBeaconsViewHolder.vDescription.setText(NOT_FOUND);
//                URL testURL = null;
////                try {
////                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(contextFragment.getActivity());
////                    testURL = new URL(sharedPreferences.getString("prefLink", "http://meschup.hcilab.org/map"));
////                    Log.i(TAG, "Database comes from " + testURL);
////                } catch (MalformedURLException e) {
////                    e.printStackTrace();
////                }
//
////                jsonLoader.download(testURL, true, contextFragment.getActivity());
//            }
//        }
//
//        @Override
//        public void onLoaderReset(Loader<LocationInfo> loader) {
//
//        }
//    }

    private String readRssi(int rssi) {
//        rssi = Math.abs(rssi);
        String hint = "out of range";
        if (rssi > RangeThreshold.NEAR) {
            hint = "very close";
        } else if (rssi > RangeThreshold.MIDDLE) {
            hint = "near";
        } else if (rssi > RangeThreshold.FAR) {
            hint = "in range";
        }
        return hint;
    }

    private class ActionExpand implements View.OnClickListener {

        public BeaconsViewHolder vh;

        private SoundPoolPlayer player;

        public View.OnClickListener setVh(BeaconsViewHolder beaconsViewHolder) {
            this.vh = beaconsViewHolder;
            return this;
        }

        @Override
        public void onClick(View v) {
            player = SoundPoolPlayer.getInstance(v.getContext());
            Log.d(TAG, "now touched in View");
//            player.play(R.raw.expand);
            readTheViewGroup(vh.vExpandArea);
        }

        private void readTheViewGroup(ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof ViewGroup) {
                    readTheViewGroup((ViewGroup) child);
                } else if (child instanceof TextView) {
                    TextView textView = (TextView) child;
//                        tts.queueRead(textView.getText().toString());
                }
            }

        }
    }
}
