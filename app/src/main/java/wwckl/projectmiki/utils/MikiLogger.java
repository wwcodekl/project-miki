package wwckl.projectmiki.utils;

import android.util.Log;

/**
 * Created by renu.yadav on 28/4/17.
 */

public class MikiLogger {
    public static boolean DEBUG = false; // will be used later

    public static void debug(String tag, String message) {
        Log.d(tag, message);
    }

    public static void error(String tag, String message) {
        Log.e(tag, message);
    }

    public static void warn(String tag, String message) {
        Log.w(tag, message);
    }

    public static void verbose(String tag, String message) {
        Log.v(tag, message);
    }

    public static void info(String tag, String message) {
        Log.i(tag, message);
    }
}
