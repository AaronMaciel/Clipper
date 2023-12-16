package ca.zgrs.clipper;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

/*
 * Receives broadcast commands and controls clipboard accordingly.
 * The broadcast receiver is active only as long as the application, or its service is active.
 */
public class ClipperReceiver extends BroadcastReceiver {
    private static String TAG = "ClipboardReceiver";

    public static String ACTION_GET = "clipper.get";
    public static String ACTION_GET_SHORT = "get";
    public static String ACTION_SET = "clipper.set";
    public static String ACTION_SET_SHORT = "set";
    public static String EXTRA_TEXT = "text";

    public static boolean isActionGet(final String action) {
        return ACTION_GET.equals(action) || ACTION_GET_SHORT.equals(action);
    }

    public static boolean isActionSet(final String action) {
        return ACTION_SET.equals(action) || ACTION_SET_SHORT.equals(action);
    }

    public static void post(String comm){
        try {
            String Command = "echo " + comm;
            Log.d(TAG,"Sent command: " + Command);
            Process process = Runtime.getRuntime().exec(Command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            Log.d(TAG,"Received command: " + line);
            while (line != null){
                System.out.println(line);
                line = reader.readLine();
                Log.d(TAG,"Received command: " + line);
            }
            int exitCode = process.waitFor();
            System.out.println("Command executed with exit code: " + exitCode);

        }   catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Receiver called");
        ClipboardManager cb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (isActionSet(intent.getAction())) {
            Log.d(TAG, "Setting text into clipboard");
            String text = intent.getStringExtra(EXTRA_TEXT);
            ClipData clipData = ClipData.newPlainText(TAG,text);
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
            CharSequence clip = clipData.getItemAt(0).getText();
            if (clip != null) {
                Log.d(TAG, String.format("Clipboard text: %s", clip));
                post(clip.toString());
                setResultCode(Activity.RESULT_OK);
                setResultData(clip.toString());
            } else {
                setResultCode(Activity.RESULT_CANCELED);
                setResultData("");
            }
        }
    }
}
