package de.uni_stuttgart.mci.bluecon;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by florian on 01.12.15.
 */
public class BeaconsViewHolder extends RecyclerView.ViewHolder {

    public TextView vName;
    public TextView vRSSI;
    public TextView vRSSI_details;
    public TextView vCategory;
    public TextView vDescription;
    public TextView vSubcategory;
    public LinearLayout vExpandArea;

    private static String TAG = "BeaconViewHolder";

    public  BeaconsViewHolder (View view) {
        super(view);

        vName = (TextView) view.findViewById(R.id.beacon_item_name);
        vRSSI = (TextView) view.findViewById(R.id.beacon_item_RSSI);
        vRSSI_details = (TextView) view.findViewById(R.id.beacon_item_RSSI_details);
        vCategory = (TextView) view.findViewById(R.id.beacon_item_category);
        vDescription = (TextView) view.findViewById(R.id.beacon_item_description);
        vSubcategory = (TextView) view.findViewById(R.id.beacon_item_subcategory);
        vExpandArea = (LinearLayout) view.findViewById(R.id.expandArea);
    }
}
