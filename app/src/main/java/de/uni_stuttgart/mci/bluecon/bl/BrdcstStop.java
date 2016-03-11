package de.uni_stuttgart.mci.bluecon.bl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.uni_stuttgart.mci.bluecon.bl.BlueconService;

/**
 * Created by florian on 20.01.16.
 */
public class BrdcstStop extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.stopService(new Intent(context, BlueconService.class));
    }
}
