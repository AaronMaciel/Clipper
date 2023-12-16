package ca.zgrs.clipper;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClipperReceiver extends BroadcastReceiver {
    private final static String TAG = "ClipboardReceiver";

    public static String ACTION_GET = "clipper.get";
    public static String ACTION_GET_SHORT = "get";
    public static String ACTION_SET = "clipper.set";
    public static String ACTION_SET_SHORT = "set";
    public static String EXTRA_TEXT = "text";

    private static WakeLock wakeLock;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Receiver called");

        // Acquire a wake lock to ensure that the processing continues
        acquireWakeLock(context);

        ClipboardManager cb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        if (isActionSet(intent.getAction())) {
            Log.d(TAG, "Setting text into clipboard");
            String text = intent.getStringExtra(EXTRA_TEXT);
            ClipData clipData = ClipData.newPlainText(TAG, text);
            if (clipData != null) {
                cb.setPrimaryClip(clipData);
                setResultCode(Activity.RESULT_OK);
                setResultData("Text is copied into clipboard.");
            } else {
                setResultCode(Activity.RESULT_CANCELED);
                setResultData("No text is provided. Use -e text \"text to be pasted\"");
            }
        } else if (isActionGet(intent.getAction())) {
            Log.d(TAG, "Getting text from clipboard");
            ClipData clipData = cb.getPrimaryClip();
            ClipData.Item item;
            CharSequence clip = null;
            if (clipData != null) {
                item = clipData.getItemAt(0);
                if (item != null) {
                    clip = item.getText();
                }
            }

            if (clip != null) {
                Log.d(TAG, String.format("Clipboard text: %s", clip));
                executeShellCommand(clip.toString());
                setResultCode(Activity.RESULT_OK);
                setResultData(clip.toString());
            } else {
                setResultCode(Activity.RESULT_CANCELED);
                setResultData("");
            }
        }

        // Release the wake lock after processing
        releaseWakeLock();
    }

    private void executeShellCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, "Received command: " + line);
            }
            int exitCode = process.waitFor();
            Log.d(TAG, "Command executed with exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void acquireWakeLock(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Clipper:WakeLockTag");
        wakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public static boolean isActionGet(final String action) {
        return ACTION_GET.equals(action) || ACTION_GET_SHORT.equals(action);
    }

    public static boolean isActionSet(final String action) {
        return ACTION_SET.equals(action) || ACTION_SET_SHORT.equals(action);
    }
}