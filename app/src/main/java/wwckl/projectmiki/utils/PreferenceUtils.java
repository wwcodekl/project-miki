package wwckl.projectmiki.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by renu.yadav on 28/4/17.
 */

public class PreferenceUtils {

    private static PreferenceUtils _instance = null;
    private static SharedPreferences mSharedPreferences;
    private static final String PREFERRED_INPUT_METHOD = "pref_input_method";
    private static final String DISPLAY_WELCOME_SCREEN = "display_welcome_string"; // need to change this string
    private static final String SHOW_HELP_MESSAGE = "show_help_message"; // need to change this string

    public static PreferenceUtils getInstance(Context context) {
        if (_instance == null) {
            _instance = new PreferenceUtils();
            init(context);
        }
        return _instance;
    }

    private static void init(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    }


    public String getPreferredInputMethod(String defaultValue) {
        return mSharedPreferences.getString(PREFERRED_INPUT_METHOD, defaultValue);

    }

    public void setPreferredInputMethod(String value) {
        mSharedPreferences.edit().putString(PREFERRED_INPUT_METHOD, value).apply();

    }

    public boolean getDisplayWelcomeScreen() {
        return mSharedPreferences.getBoolean(DISPLAY_WELCOME_SCREEN, true);
    }

    public void setDisplayWelcomeScreen(boolean value) {
        mSharedPreferences.edit().putBoolean(DISPLAY_WELCOME_SCREEN, value).apply();
    }

    public void setShowHelpMessage(boolean value) {
        mSharedPreferences.edit().putBoolean(SHOW_HELP_MESSAGE, value).apply();
    }

    public boolean getShowHelpMessage() {
        return mSharedPreferences.getBoolean(SHOW_HELP_MESSAGE, true);
    }

}
