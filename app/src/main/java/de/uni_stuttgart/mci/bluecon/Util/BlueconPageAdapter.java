package de.uni_stuttgart.mci.bluecon.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import de.uni_stuttgart.mci.bluecon.fragments.ScanListFragment;
import de.uni_stuttgart.mci.bluecon.fragments.SearchListFragment;
import de.uni_stuttgart.mci.bluecon.fragments.NavigationListFragment;

/**
 * Created by florian on 22.10.15.
 */
public class BlueconPageAdapter extends FragmentPagerAdapter {
    ScanListFragment scanFrag;
    SearchListFragment searchFrag;
    NavigationListFragment naviFrag;

    public BlueconPageAdapter (FragmentManager fm) {
        super(fm);
//        scanFrag = new ScanListFragment();
        scanFrag = new ScanListFragment();
        searchFrag = new SearchListFragment();
        naviFrag = new NavigationListFragment();
    }

    @Override
    public Fragment getItem (int i) {
        switch (i){
            case 0:
                return scanFrag;
            case 1:
                return searchFrag;
            case 2:
                return naviFrag;
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "Scan";
            case 1:
                return "Search";
            case 2:
                return "NaviFrag";
            default:
                return null;
    }
    }

    @Override
    public int getCount() {return 3;}
}
