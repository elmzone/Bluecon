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
public class BeaconsViewHolderOld extends RecyclerView.ViewHolder {

    public View parent;
    public TextView vName;
    public TextView vRSSI;
    public TextView vRSSI_details;
    public TextView vCategory;
    public TextView vDescription;
    public TextView vSubcategory;
    public LinearLayout vExpandArea;
    public Button btnBeep;
    public Button btnSonar;

    private static String TAG = "BeaconViewHolder";

    public BeaconsViewHolderOld(View view) {
        super(view);

        parent = view;
        vName = (TextView) view.findViewById(R.id.beacon_item_name);
        vRSSI = (TextView) view.findViewById(R.id.beacon_item_RSSI);
        vRSSI_details = (TextView) view.findViewById(R.id.beacon_item_RSSI_details);
//        vRoomId = (TextView) view.findViewById(R.id.beacon_item_category);
        vDescription = (TextView) view.findViewById(R.id.beacon_item_description);
//        vPlaceId = (TextView) view.findViewById(R.id.beacon_item_subcategory);
        vExpandArea = (LinearLayout) view.findViewById(R.id.expandArea);

        btnBeep = (Button) view.findViewById(R.id.beacon_beep);
        btnSonar = (Button) view.findViewById(R.id.beacon_sonar);
    }
}
