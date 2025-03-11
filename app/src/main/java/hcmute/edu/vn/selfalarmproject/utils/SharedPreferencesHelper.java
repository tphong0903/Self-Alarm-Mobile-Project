package hcmute.edu.vn.selfalarmproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String PREF_NAME = "Chat";
    private static final String KEY_GOOGLE_UID = "GOOGLE_UID";

    public static void saveGoogleUid(Context context, String googleUid) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_GOOGLE_UID, googleUid);
        editor.apply();
    }

    public static String getGoogleUid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_GOOGLE_UID, null);
    }

    public static void clearGoogleUid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_GOOGLE_UID);
        editor.apply();
    }
}
