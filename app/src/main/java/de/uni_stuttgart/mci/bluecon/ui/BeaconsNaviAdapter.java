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
import de.uni_stuttgart.mci.bluecon.util2.IResultListener;
import de.uni_stuttgart.mci.bluecon.util2.SoundPoolPlayer;

public class BeaconsNaviAdapter extends Adapter<BeaconsViewHolder> {

    private List<BeaconLocation> beaconsList;
    private static final String TAG = "BeaconsAdapter";
    private IResultListener<BeaconLocation> listener;

    private Context context;

    public BeaconsNaviAdapter setResultListener(IResultListener<BeaconLocation> IResultListener) {
        this.listener = IResultListener;
        return this;
    }

    public List<BeaconLocation> getBeaconsList() {
        return beaconsList;
    }

    public BeaconsNaviAdapter(List<BeaconLocation> beaconsMap) {
        this.beaconsList = beaconsMap;
//        this.beaconDBHelper = beaconDBHelper;
//        jsonLoader = JSONLoader.getInstance(beaconDBHelper);
    }

    @Override
    public BeaconsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.beacon_layout_navi, parent, false);
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
//        vh.vRSSI_details.setText(String.valueOf(beaconLocation.RSSI));
        vh.vPlaceId.setText("");
        vh.vRoomId.setText(context.getString(R.string.txt_navi_room) + beaconLocation.roomId);
        vh.vDescription.setText(beaconLocation.description);
        if (beaconLocation.nextBeacon.equals(BeaconLocation.NO_NEXT_BEACON)) {
            vh.vToNext.setText(R.string.txt_navi_target_reached);
        } else {
            vh.vToNext.setText(context.getString(R.string.txt_navi_way_to_room) + beaconLocation.nextBeacon + " : " + beaconLocation.neighborhood.get(beaconLocation.nextBeacon).wayToIt);
        }

        vh.btnBeep.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(new Intent(v.getContext().getString(R.string.intent_gatt_open)).putExtra(v.getContext().getString(R.string.bndl_mac), beaconLocation.macAddress));

                                          }
                                      }
        );
//        vh.itemView.setOnClickListener(new View.OnClickListener() {
//
//            public BeaconLocation b;
//
//            public View.OnClickListener init(BeaconLocation beaconLocation) {
////                this.b = beaconLocation;
//                return this;
//            }
//
//
//            @Override
//            public void onClick(View v) {
////                listener.onResult(b);
//            }
//        }.init(beaconLocation));


        ActionExpand a = (ActionExpand) vh.parent.getTag();
        a.setVh(vh);
    }

    @Override
    public int getItemCount() {
        return beaconsList.size();
    }

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
