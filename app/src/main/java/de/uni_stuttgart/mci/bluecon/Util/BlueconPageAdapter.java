package de.uni_stuttgart.mci.bluecon.Util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.uni_stuttgart.mci.bluecon.scan.ScanListFragment;
import de.uni_stuttgart.mci.bluecon.search.SearchListFragment;

/**
 * Created by florian on 22.10.15.
 */
public class BlueconPageAdapter extends FragmentPagerAdapter {

    ScanListFragment scanFrag;
    SearchListFragment searchFrag;

    public BlueconPageAdapter (FragmentManager fm) {
        super(fm);
//        scanFrag = new ScanListFragment();
        scanFrag = new ScanListFragment();
        searchFrag = new SearchListFragment();
    }

    @Override
    public Fragment getItem (int i) {
        switch (i){
            case 0:
                return scanFrag;
            case 1:
                return searchFrag;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {return 2;}
}
