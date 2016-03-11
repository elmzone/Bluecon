package de.uni_stuttgart.mci.bluecon.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import de.uni_stuttgart.mci.bluecon.util.IResultListener;
import de.uni_stuttgart.mci.bluecon.util.RecyclerItemClickListener;

public class DiagAllBeaconChoose extends DialogFragment{

    private RecyclerView mRecyclerView;
    private BeaconsSearchAdapter adapter;
    private EditText editText;
    private Button searchButton;

    private static String TAG = "SearchListFragment";
    private IResultListener<BeaconLocation> resultListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//TODO

//        setHasOptionsMenu(true);//default is false;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //assign DOM element
        View rootView = inflater.inflate(R.layout.search_fragment_main, null,
                false);
        builder.setView(rootView);
        builder.setTitle(R.string.txt_dia_choose_beacon);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.search_recycler_view);
        registerForContextMenu(mRecyclerView);

        initRecyclerView(mRecyclerView);



        //set the adapter for the list
        adapter = new BeaconsSearchAdapter(new ArrayList<BeaconLocation>());
        adapter.setResultListener(new IResultListener<BeaconLocation>() {
            @Override
            public void onResult(BeaconLocation result) {
                resultListener.onResult(result);
                dismiss();
            }
        });
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
        return builder.create();


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

    public DiagAllBeaconChoose setResultListener(IResultListener<BeaconLocation> resultListener) {
        this.resultListener = resultListener;
        return this;
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
