package xyz.shurlin.demo2;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public class MyApplication extends Application {

    public static final String KEY_NIGHT_MODE = "pref_night_mode";
    // 存储值: "light" / "dark" / "system"

    @Override
    public void onCreate() {
        super.onCreate();
        applySavedNightMode();
    }

    private void applySavedNightMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String mode = prefs.getString(KEY_NIGHT_MODE, "system");
        setNightModeFromString(mode);
    }

    public static void setNightModeFromString(String mode) {
        switch (mode) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                // Follow system: supported on AndroidX; maps to system default behavior
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
