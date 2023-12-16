package ca.zgrs.clipper;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class ClipboardService extends JobService {
    private static final String TAG = "ClipboardService";
    private PowerManager.WakeLock wakeLock;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Start clipboard service.");

        // Ensure that the service is kept alive
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "ClipboardService::Wakelock"
        );
        wakeLock.acquire(2000);

        // Perform your background processing logic here

        // Return true if the job needs to be rescheduled
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Release the wake lock when the job is stopped
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        return false; // Don't reschedule the job
    }

    // Helper method to enqueue work to the service
    public static void enqueueWork(Context context, Intent work) {
        // Schedule your job here
    }
}