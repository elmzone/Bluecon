package de.uni_stuttgart.mci.bluecon;

import android.app.Application;

import java.io.IOException;

import de.uni_stuttgart.mci.bluecon.util.ParserUtil;
import de.uni_stuttgart.mci.bluecon.util.ParserUtilOld;

/**
 * Created by flori_000 on 26.01.2016.
 */
public class BlueconApp extends Application {

    private static BlueconApp inst;

    @Override
    public void onCreate() {
        super.onCreate();
         inst = this;


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BeaconHolder.inst().addLocationInfo(ParserUtilOld.parseLocation(getResources().openRawResource(R.raw.oldjson)));
                    BeaconHolder.inst().addBeaconLocations(ParserUtil.parseLocation(getResources().openRawResource(R.raw.firstfloor_notready)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static BlueconApp inst(){
        return inst;
    }
}
