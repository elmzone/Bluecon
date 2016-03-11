package de.uni_stuttgart.mci.bluecon.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_stuttgart.mci.bluecon.BeaconHolder;
import de.uni_stuttgart.mci.bluecon.domain.BeaconLocation;
import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.util.IResultListener;

public class NavigationListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button bt_from;
    private Button bt_to;
    private Button bt_calc;
    private TextView fromText;
    private TextView toText;

    private BeaconLocation start = null;
    private BeaconLocation target = null;
    private List<BeaconLocation> resultList;


    public NavigationListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NavigationListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NavigationListFragment newInstance(String param1, String param2) {
        NavigationListFragment fragment = new NavigationListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.navigation_list_fragment, container, false);
        bt_from = (Button) v.findViewById(R.id.bt_from);
        bt_to = (Button) v.findViewById(R.id.bt_to);
        bt_calc = (Button) v.findViewById(R.id.bt_calc);

        fromText = (TextView) v.findViewById(R.id.beacon_item_from);
        toText = (TextView) v.findViewById(R.id.beacon_item_to);


        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        bt_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DiagBeaconChoose().setResultListener(new IResultListener<BeaconLocation>() {
                    @Override
                    public void onResult(BeaconLocation result) {
                        start = result;
                        fromText.setText(result.placeId);
                    }
                }).show(getActivity().getSupportFragmentManager(), "dialog");
            }
        });

        bt_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DiagAllBeaconChoose().setResultListener(new IResultListener<BeaconLocation>() {
                    @Override
                    public void onResult(BeaconLocation result) {
                        target = result;
                        toText.setText(result.placeId);
                    }
                }).show(getActivity().getSupportFragmentManager(), "dialog");
            }
        });

        bt_calc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start != null && target != null) {
                    new CalculateShortestPath().execute(start, target);
                }
            }
        });

    }
// Tiefensuche eine Ebene nach der anderen ausgehend vom Startknoten wird durchsucht
    private class CalculateShortestPath extends AsyncTask<BeaconLocation, Void, List<BeaconLocation>> {
        List<BeaconLocation> allBeacons = new ArrayList<BeaconLocation>();



        @Override
        protected List<BeaconLocation> doInBackground(BeaconLocation[] params) {
            allBeacons = BeaconHolder.beaconLocations();
            Map<String,BeaconLocation> allBeaconsMap = new HashMap<>();
            for (BeaconLocation aB : allBeacons) {
                allBeaconsMap.put(aB.roomId, aB);
            }

            SearchObject startObj = new SearchObject();
            startObj.active = start;
            startObj.path.add(start);
            List<SearchObject> oneLevel = new ArrayList<>();
            oneLevel.add(startObj);
            List<SearchObject> next = new ArrayList<>();

            while(true) {
                for (SearchObject ol : oneLevel) {
                    Set neighbors = ol.active.neighborhood.keySet();
                    for (Object n : neighbors) {
                        if (!ol.pre.roomId.equals(n)){
                            SearchObject nextObj = new SearchObject();
                            nextObj.active = allBeaconsMap.get(n);
                            nextObj.pre = ol.active;
                            nextObj.path.add(nextObj.active);
                            next.add(nextObj);
                        }
                        if (target.roomId.equals(n))
                            break;
                    }

                }
                oneLevel = next;
            }


        }

        @Override
        protected void onPostExecute(List<BeaconLocation> list) {
            NavigationListFragment.this.fromText.setText("feddisch!");
            for (BeaconLocation r : resultList) {

                Log.d("NavFrag", r.roomId);
            }
        }

       private class SearchObject{
           public BeaconLocation active;
           public BeaconLocation pre;
           public List<BeaconLocation> path;
       }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
