package de.uni_stuttgart.mci.bluecon.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Dridiger
 */
public class ScheduleUtil {

    public static void scheduleWork(Runnable work, long time, TimeUnit unit) {
        ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
        worker.schedule(work, time, unit);
    }
}
