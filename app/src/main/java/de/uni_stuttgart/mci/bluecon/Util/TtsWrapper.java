package de.uni_stuttgart.mci.bluecon.Util;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.uni_stuttgart.mci.bluecon.R;

public class TtsWrapper implements TextToSpeech.OnInitListener, ITtsProvider {

    private static final String TAG = "TTS";
    private static TtsWrapper inst;

    private boolean init = false;
    private boolean access = true;
    private TextToSpeech tts;

    private List<String> queue = new ArrayList<>();
    private List<ITtsUser> ttsUsers = new ArrayList<>();

    private TtsWrapper(Context context) {
        inst = this;
//        if (checkAccessibility(context)) {
        tts = new TextToSpeech(context, inst);
        access = true;
//        }
    }

    public static void init(Context c) {
        if (inst == null) {
            new TtsWrapper(c);
        }
    }

    public static TtsWrapper inst() {
        return inst;
    }

    public static boolean exists() {
        return inst != null;
    }


    private boolean checkAccessibility(Context c) {
        AccessibilityManager am = (AccessibilityManager) c.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return am.isEnabled() | am.isTouchExplorationEnabled();
    }

    @Override
    public void onInit(int status) {
        init = true;
        Log.e(TAG, "onInit: ");
        if (!queue.isEmpty()) {
            readOut(queue);
        }
    }

    @Override
    public void queueRead(String read) {
        if (access) {
            if (init) {
                readOut(read);
            } else {
                queue.add(read);
            }
        }
    }

    @Override
    public void queueRead(String... read) {
        if (access) {
            if (init) {
                for (String r : read) {
                    readOut(r);
                }
            }
        }
    }

    private void readOut(List<String> read) {
        if (access) {
            for (String r : read) {
                readOut(r);
            }
        }
    }

    private void readOut(String read) {
        Runnable focus = new Runnable() {
            public String read;

            public Runnable init(String read) {
                this.read = read;
                return this;
            }

            @Override
            public void run() {
                if (access) {
                    tts.speak(read, TextToSpeech.QUEUE_ADD, null, read);
                }
            }
        }.init(read);
        Executors.newSingleThreadScheduledExecutor().schedule(focus, 500, TimeUnit.MILLISECONDS);
    }

    public void shutDown() {
        if (access) {
            tts.shutdown();
        }
    }

    public ITtsProvider registerUser(ITtsUser user) {
        ttsUsers.add(user);
        return this;
    }

    public void deregisterUser(ITtsUser user) {
        ttsUsers.remove(user);
        if (ttsUsers.isEmpty()) {
            shutDown();
        }
    }

    public interface ITtsUser {

    }
}