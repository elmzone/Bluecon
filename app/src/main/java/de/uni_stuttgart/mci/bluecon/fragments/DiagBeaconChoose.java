package de.uni_stuttgart.mci.bluecon.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.uni_stuttgart.mci.bluecon.BeaconHolder;
import de.uni_stuttgart.mci.bluecon.domain.BeaconLocation;
import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.util2.IResultListener;

/**
 * Created by flori_000 on 02.03.2016.
 */
public class DiagBeaconChoose extends DialogFragment {
    private ListView beaconlist;
    private IResultListener<BeaconLocation> resultListener;

    public DiagBeaconChoose setResultListener(IResultListener<BeaconLocation> IResultListener) {
        this.resultListener = IResultListener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_choose_beacon, null);
        builder.setView(v);
        builder.setTitle(R.string.txt_dia_choose_beacon);
        beaconlist = (ListView) v.findViewById(R.id.list_beacon_choose);

        ArrayAdapter<BeaconLocation> adapter = new AdapterBeacons(getActivity(), R.id.beacon_item_name, new ArrayList<>(BeaconHolder.beacons()));

        beaconlist.setAdapter(adapter);

        beaconlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BeaconLocation beaconLocation = (BeaconLocation) parent.getItemAtPosition(position);
                resultListener.onResult(beaconLocation);
                dismiss();
            }
        });

        return builder.create();
    }


    private class AdapterBeacons extends ArrayAdapter<BeaconLocation> {

        public AdapterBeacons(Context context, int resource, List<BeaconLocation> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BeaconLocation beacon = getItem(position);
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.litem_beacon, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.beacon_item_name);
                holder.description = (TextView) convertView.findViewById(R.id.beacon_item_description);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(beacon.placeId);
//            holder.description.setText(beacon.describeContents());
            return convertView;
        }

        class ViewHolder {
            TextView name;
            TextView description;
        }
    }
}
