package de.uni_stuttgart.mci.bluecon;

import android.app.Application;

/**
 * Created by flori_000 on 26.01.2016.
 */
public class BlueconApp extends Application {

    private static BlueconApp inst;

    @Override
    public void onCreate() {
        super.onCreate();

         inst = this;

    }

    public static BlueconApp inst(){
        return inst;
    }
}
