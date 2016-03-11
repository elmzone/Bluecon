package de.uni_stuttgart.mci.bluecon.act;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import de.uni_stuttgart.mci.bluecon.R;
import de.uni_stuttgart.mci.bluecon.network.JSONLoader;

/**
 * Created by florian on 01.12.15.
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = "SettingActivity";

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().add(android.R.id.content, new SettingsFragment()).commit();

        if (hasHeaders()) {
            Button button = new Button(this);
            button.setText(R.string.Settings_Activity_Name);
            setListFooter(button);
        }
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private ListPreference frequencyPref;
        private EditTextPreference thresholdPref;
        private EditTextPreference linkPref;
        private EditTextPreference beepDuraPref;
        private EditTextPreference beepFreqPref;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.settings, false);
            addPreferencesFromResource(R.xml.settings);

            frequencyPref = (ListPreference) findPreference(getString(R.string.cnst_period));
            frequencyPref.setDefaultValue(1);
            frequencyPref.setTitle(getString(R.string.pref_frequency_summary) + " : " + frequencyPref.getEntry());

            beepFreqPref = (EditTextPreference) findPreference(getString(R.string.pref_beep_freq_key));
            beepFreqPref.setDefaultValue(440);
            beepFreqPref.setTitle(getString(R.string.pref_beep_freq) + " : " + beepFreqPref.getText());

            beepDuraPref = (EditTextPreference) findPreference(getString(R.string.pref_beep_dura_key));
            beepDuraPref.setDefaultValue(2);
            beepDuraPref.setTitle(getString(R.string.pref_beep_dura) + " : " + beepDuraPref.getText());

            thresholdPref = (EditTextPreference) findPreference("prefThreshold");
            thresholdPref.setTitle(getString(R.string.pref_frequency_summary) + " : " + thresholdPref.getText());

            linkPref = (EditTextPreference) findPreference(getString(R.string.prefs_link_url));
            createButton();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//            if (key.equals("prefThreshold")) {
//                Log.i(TAG, "preference Threshold!");
//
//                thresholdPref.setTitle(getString(R.string.pref_frequency_summary) + " : " + thresholdPref.getText());
//
//            }
//
//            if (key.equals(getString(R.string.cnst_period))) {
//                Log.i(TAG, "preference Frequency!");
//                frequencyPref.setTitle(getString(R.string.pref_frequency_summary) + " : " + frequencyPref.getEntry());
//            }

            getPreferenceScreen().removeAll();
            addPreferencesFromResource(R.xml.settings);
        }

        private void createButton() {
            Preference button = findPreference("update_button");

            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    try {
                        URL sourceUrl = new URL(linkPref.getText());

                        JSONLoader.getInstance(null).download(sourceUrl, false, getActivity());
                    } catch (MalformedURLException e) {
                        Toast.makeText(getActivity(), R.string.url_not_avaliable, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    return true;
                }
            });
        }


    }
}
