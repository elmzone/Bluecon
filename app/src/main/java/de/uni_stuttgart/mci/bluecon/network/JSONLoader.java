package de.uni_stuttgart.mci.bluecon.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.uni_stuttgart.mci.bluecon.database.BeaconDBHelper;
import de.uni_stuttgart.mci.bluecon.database.DatabaseUtil;
import de.uni_stuttgart.mci.bluecon.database.LocationInfo;

/**
 * Created by florian on 02.12.15.
 */
public class JSONLoader {
    private static String TAG = "JSONLoader";
    private static JSONLoader jsonLoader;
    private BeaconDBHelper beaconDBHelper;
    private Context context;

    public static JSONLoader getInstance(BeaconDBHelper beaconDBHelper) {
        if (jsonLoader != null) {
            return jsonLoader;
        } else if (beaconDBHelper != null) {
            jsonLoader = new JSONLoader(beaconDBHelper);
            return jsonLoader;
        } else  {
            return null;
        }

    }

    private JSONLoader (BeaconDBHelper beaconDBHelper) {
        this.beaconDBHelper = beaconDBHelper;
    }

    public void download (URL url, boolean shouldCheck, Context context) {
        this.context = context;
        new jsonLoaderTask().execute(url, shouldCheck);
    }

    private class jsonLoaderTask extends AsyncTask<Object, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            boolean result = false;

            try {
                result = downloadURL((URL)params[0],(boolean)params[1]);
            }catch (IOException e) {
                e.printStackTrace();
            }
            if (isCancelled()) {
                // TODO
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Log.d(TAG, "the download result is " + aBoolean);
            if (aBoolean) {
                Toast.makeText(context.getApplicationContext(), "download completed", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aBoolean);
        }
    }

    private boolean downloadURL (URL url, boolean shouldCheck) throws IOException {
        InputStream inputStream = null;

        if (!shouldCheck || !DatabaseUtil.queryURL(this.beaconDBHelper, url.toString())) {
            try {
                Log.i(TAG, "has not visited , start download");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                inputStream = conn.getInputStream();

                // Convert the InputStream into a string
                if(inputStream != null) {
                    List<LocationInfo> locationInfos = ParserUtil.parseLocation(inputStream);
                    for (LocationInfo locationInfo : locationInfos) {
                        long rowNum = DatabaseUtil.insertData(beaconDBHelper, locationInfo);
                        Log.d(TAG, "insert a new row, row number is" + rowNum);
                    }
                }
                long rowNumber = DatabaseUtil.insertURL(beaconDBHelper, url.toString());
                Log.d(TAG, "inset new row in url table, now the size is" + rowNumber);
                return true;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }else {
            Log.i(TAG, "url is already visited once");
            return false;
        }
    }
}
