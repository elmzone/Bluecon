package de.uni_stuttgart.mci.bluecon.act;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.util.DialogCreator;

//import com.drisoftie.frags.comp.ManagedActivity;

public abstract class ActBasePerms extends AppCompatActivity {

    public boolean ok;
    protected BluetoothAdapter blAdapt;

    @Override
    protected void onResume() {
        super.onResume();
        blAdapt = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (blAdapt != null) {
            // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
            // fire an intent to display a dialog asking the user to grant permission to enable it.
            //noinspection ResourceType
            if (blAdapt.isEnabled()) {
                onBlAdaptStarted();
            } else {
                ok = false;
                DialogCreator.createDialogAlert(this, R.string.txt_bl_requ_turn_on, R.string.txt_bl_descr_requ_turn_on, R.string.txt_ok,
                        new DiagBlOkTurnOn(), R.string.txt_close, new DiagBlClose(), new DiagDismissDefault());
            }
        } else {
            DialogCreator.createDialogAlert(this, R.string.txt_bl_needed_then_stop, R.string.txt_bl_descr_needed_then_stop,
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    onBlAdaptStarted();
                } else {
                    ok = false;
                    DialogCreator.createDialogAlert(this, R.string.txt_bl_requ_turn_on, R.string.txt_bl_descr_requ_turn_on, R.string.txt_ok,
                            new DiagBlOkTurnOn(), R.string.txt_close, new DiagBlClose(), new DiagDismissDefault());
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void checkPermLocation() {
        int permScanCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        switch (permScanCheck) {
            case PackageManager.PERMISSION_GRANTED: {
                startBlServiceConn();
                break;
            }
            case PackageManager.PERMISSION_DENIED: // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    ok = false;
                    DialogCreator.createDialogAlert(this, R.string.txt_bl_loc_req_turn_on, R.string.txt_bl_descr_loc_requ_turn_on,
                            R.string.txt_ok, new DiagBlOkPerm(), R.string.txt_close, new DiagBlClose(),
                            new DiagDismissDefault());
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            1);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 1) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBlServiceConn();
            } else {
                ok = false;
                DialogCreator.createDialogAlert(this, R.string.txt_bl_loc_req_turn_on, R.string.txt_bl_descr_loc_turn_on_last,
                        R.string.txt_ok, new DiagBlOkPerm(), R.string.txt_close, new DiagBlClose(),
                        new DiagDismissDefault());
            }
        }
    }

    protected abstract void startBlServiceConn();

    protected abstract void onBlAdaptStarted();

    private class DiagBlOkTurnOn implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            ok = true;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    private class DiagBlOkPerm implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            ok = true;
            ActivityCompat.requestPermissions(ActBasePerms.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    private class DiagBlClose implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    }

    private class DiagDismissDefault implements DialogInterface.OnDismissListener {

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (!ok) {
                finish();
            }
        }
    }
}
