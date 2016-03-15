package de.uni_stuttgart.mci.bluecon.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.uni_stuttgart.mci.bluecon.R;

/**
 * Created by florian on 01.12.15.
 */
public class BeaconsViewHolder extends RecyclerView.ViewHolder {

    public boolean expanded;
    public View parent;
    public TextView vName;
    public TextView vRSSI;
    public TextView vRSSI_details;
    public TextView vRoomId;
    public TextView vDescription;
    public TextView vPlaceId;
    public TextView vToNext;
    public LinearLayout vExpandArea;
    public Button btnBeep;
    public Button btnSonar;

    public int heightOriginal = 0;
    public int heightExpanded = 0;

    private static String TAG = "BeaconViewHolder";

    public BeaconsViewHolder(View view) {
        super(view);

        parent = view;
        vName = (TextView) view.findViewById(R.id.beacon_item_name);
        vRSSI = (TextView) view.findViewById(R.id.beacon_item_RSSI);
        vRSSI_details = (TextView) view.findViewById(R.id.beacon_item_RSSI_details);
        vRoomId = (TextView) view.findViewById(R.id.beacon_item_roomId);
        vDescription = (TextView) view.findViewById(R.id.beacon_item_description);
        vPlaceId = (TextView) view.findViewById(R.id.beacon_item_placeId);
        vExpandArea = (LinearLayout) view.findViewById(R.id.expandArea);
        vToNext = (TextView) view.findViewById(R.id.beacon_item_way_to_next);

        btnBeep = (Button) view.findViewById(R.id.beacon_beep);
//        btnSonar = (Button) view.findViewById(R.id.beacon_sonar);
    }
}
