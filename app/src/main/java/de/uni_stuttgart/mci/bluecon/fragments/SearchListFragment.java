package de.uni_stuttgart.mci.bluecon.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import de.uni_stuttgart.mci.bluecon.BeaconHolder;
import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.domain.BeaconLocation;
import de.uni_stuttgart.mci.bluecon.ui.BeaconsAdapter;
import de.uni_stuttgart.mci.bluecon.ui.BeaconsSearchAdapter;

public class SearchListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private BeaconsSearchAdapter adapter;
    private EditText editText;
    private Button searchButton;

    private static String TAG = "SearchListFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//TODO

//        setHasOptionsMenu(true);//default is false;

        //assign DOM element
        View rootView = inflater.inflate(R.layout.search_fragment_main, container,
                false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.search_recycler_view);
        registerForContextMenu(mRecyclerView);

        initRecyclerView(mRecyclerView);


        //set the adapter for the list
        adapter = new BeaconsSearchAdapter(new ArrayList<BeaconLocation>());
        mRecyclerView.setAdapter(adapter);


//================================= UI listeners =======================================

        searchButton = (Button) rootView.findViewById(R.id.search_button);
        editText = (EditText) rootView.findViewById(R.id.editText);

        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String search = editText.getText().toString().trim();
                adapter.getBeaconsList().clear();
                adapter.getBeaconsList().addAll(BeaconHolder.inst().searchForBeacons(search));
                        adapter.notifyDataSetChanged();
//                getLoaderManager().initLoader(loaderID, bundle);
//                loaderID++;
            }
        });
        return rootView;


    }

//============================================Helper Function===========================================
//======================================================================================================

    /**
     * set the focus function and layoutManager of the recycler view
     *
     * @param recyclerView target view
     */
    private void initRecyclerView(RecyclerView recyclerView) {
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        recyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        ((LinearLayoutManager) mLayoutManager).setOrientation(LinearLayout.VERTICAL);
    }

////===========================================DataLoader Callback========================================
////======================================================================================================
//
//    private class BeaconDataLoaderCallbacks implements LoaderManager.LoaderCallbacks<ArrayList<LocationInfo>> {
//        private String keyword;
//
//        @Override
//        public Loader<ArrayList<LocationInfo>> onCreateLoader(int id, Bundle args) {
//            this.keyword = args.getString("keyword");
//            return new BeaconSearchLoader(getActivity(), beaconDBHelper, keyword);
//        }
//
//        @Override
//        public void onLoadFinished(Loader<ArrayList<LocationInfo>> loader, ArrayList<LocationInfo> data) {
//            beaconsList.clear();
//            if(data!= null){
//                for (LocationInfo location : data){
//                    beaconsList.add(location);
//                }
//            }
//            Log.d(TAG, "now the beaconsList is" + beaconsList);
//            mRecyclerView.setAdapter(adapter);
//            adapter.notifyDataSetChanged();
//        }
//
//        @Override
//        public void onLoaderReset(Loader<ArrayList<LocationInfo>> arrayListLoader) {
//            //do nothing.
//        }
//
//    }
}
