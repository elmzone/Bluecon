package de.uni_stuttgart.mci.bluecon.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_stuttgart.mci.bluecon.BeaconHolder;
import de.uni_stuttgart.mci.bluecon.domain.BeaconLocation;
import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.domain.RangeThreshold;
import de.uni_stuttgart.mci.bluecon.ui.BeaconsNaviAdapter;
import de.uni_stuttgart.mci.bluecon.ui.BeaconsViewHolder;
import de.uni_stuttgart.mci.bluecon.util.IResultListener;

public class NavigationListFragment extends Fragment implements BeaconHolder.IBeaconListener {
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
    private List<BeaconLocation> resultList = new ArrayList<>();


    private RecyclerView mRecyclerView;
    private BeaconsNaviAdapter mAdapter;


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

        mRecyclerView = (RecyclerView) v.findViewById(R.id.navi_recycler_view);
        initRecyclerView(mRecyclerView);


//        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            //Does nothing :)
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            // Enables Swiping only when at Top of the list
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                boolean enable = false;
//                if (mRecyclerView != null && mRecyclerView.getChildCount() > 0) {
//                    enable = mRecyclerView.getChildAt(0).getTop() == 0;
//                }
//            }
//        });


        mAdapter = new BeaconsNaviAdapter(resultList);
        mRecyclerView.setAdapter(mAdapter);

        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        BeaconHolder.inst().registerBeaconListener(this);
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
                    BeaconHolder.inst().resetRSSI();
                    new CalculateShortestPath().execute(start, target);
                } else {

                    Toast.makeText(getActivity(), R.string.txt_navi_error_no_two_selected, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        BeaconHolder.inst().deregisterBeaconListener(this);
    }

    @Override
    public void onBeaconChanged(BeaconLocation changedBeacon) {
        BeaconsViewHolder viewHolder = (BeaconsViewHolder) mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getBeaconsList().indexOf(changedBeacon));
        if (viewHolder != null && viewHolder.vRSSI != null) {
            if (changedBeacon.RSSI < RangeThreshold.NEAR) {
                viewHolder.vRSSI.setText(R.string.txt_very_close);
            } else if (changedBeacon.RSSI < RangeThreshold.MIDDLE) {
                viewHolder.vRSSI.setText(R.string.txt_near);
            } else if (changedBeacon.RSSI < RangeThreshold.FAR) {
                viewHolder.vRSSI.setText(R.string.txt_in_range);
            } else {
                viewHolder.vRSSI.setText(R.string.txt_out_of_range);
            }
//            viewHolder.vRSSI_details.setText(String.valueOf(changedBeacon.RSSI));

        }
        if (changedBeacon.preRSSI < RangeThreshold.NEAR && changedBeacon.RSSI >= RangeThreshold.NEAR) {
            changedBeacon.preRSSI = changedBeacon.RSSI;
            Toast.makeText(getActivity(), changedBeacon.placeId + this.getString(R.string.txt_is_very_close), Toast.LENGTH_SHORT).show();
        } else if (changedBeacon.preRSSI < RangeThreshold.MIDDLE && changedBeacon.RSSI >= RangeThreshold.MIDDLE) {
            changedBeacon.preRSSI = changedBeacon.RSSI;
            Toast.makeText(getActivity(), changedBeacon.placeId + this.getString(R.string.txt_is_near), Toast.LENGTH_SHORT).show();
        } else if (changedBeacon.preRSSI < RangeThreshold.FAR && changedBeacon.RSSI >= RangeThreshold.FAR) {
            changedBeacon.preRSSI = changedBeacon.RSSI;
            Toast.makeText(getActivity(), changedBeacon.placeId + this.getString(R.string.txt_is_in_range), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBeaconsAdded() {

    }

    @Override
    public void onBeaconRemoved(BeaconLocation removedBeacon) {

    }

    // BFS: One Level of the Search tree after the other will be searched fully.
    // future work: for bigger Databases Dijkstra or Floyd Warshall to calculate all shortest paths first
    private class CalculateShortestPath extends AsyncTask<BeaconLocation, Void, List<BeaconLocation>> {
        List<BeaconLocation> allBeacons = new ArrayList<BeaconLocation>();


        @Override
        protected List<BeaconLocation> doInBackground(BeaconLocation[] params) {
            start = params[0];
            target = params[1];
            allBeacons = BeaconHolder.beaconLocations();
            Map<String, BeaconLocation> allBeaconsMap = new HashMap<>();
            for (BeaconLocation aB : allBeacons) {
                allBeaconsMap.put(aB.roomId, aB);
            }


            SearchObject startObj = new SearchObject();
            startObj.active = start;
            startObj.pre = start;
            startObj.path = new ArrayList<>();
            startObj.path.add(startObj.active);
            List<SearchObject> oneLevel = new ArrayList<>();
            oneLevel.add(startObj);
            List<SearchObject> next = new ArrayList<>();
            boolean stop = false;
            while (!stop) {
                for (SearchObject ol : oneLevel) {
                    Set<String> neighbors = ol.active.neighborhood.keySet();
                    for (Object n : neighbors) {
                        if (!ol.pre.roomId.equals(n)) {
                            SearchObject nextObj = new SearchObject();
                            nextObj.active = allBeaconsMap.get(n);
                            nextObj.pre = ol.active;
                            nextObj.path = new ArrayList<>();
                            for (BeaconLocation p : ol.path) {
                                nextObj.path.add(p);
                            }
                            nextObj.path.add(nextObj.active);
                            next.add(nextObj);
//                            Log.d("NaviFrag", "added" + nextObj.active.roomId);
                        }
                        if (target.roomId.equals(n)) {
                            stop = true;
                            break;
                        }
                    }
                    if (stop)
                        break;
                }
                oneLevel.clear();
                for (SearchObject n : next) {
                    oneLevel.add(n);
                }
                next.clear();
            }
            return oneLevel.get(oneLevel.size() - 1).path;


        }

        @Override
        protected void onPostExecute(List<BeaconLocation> list) {
            resultList = list;
            for (BeaconLocation rL : resultList) {
                if (resultList.indexOf(rL) != (resultList.size() - 1)) {
                    rL.nextBeacon = resultList.get(resultList.indexOf(rL) + 1).roomId;
                }
                rL.RSSI = -200;
            }
            mAdapter.getBeaconsList().clear();
            mAdapter.getBeaconsList().addAll(resultList);
            mAdapter.notifyDataSetChanged();

            Toast.makeText(getActivity(), R.string.txt_navi_toast_calc_succ, Toast.LENGTH_SHORT).show();
            return;
        }

        private class SearchObject {
            public BeaconLocation active;
            public BeaconLocation pre;
            public List<BeaconLocation> path;
        }
    }

    private void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        ((LinearLayoutManager) mLayoutManager).setOrientation(LinearLayoutManager.VERTICAL);
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
