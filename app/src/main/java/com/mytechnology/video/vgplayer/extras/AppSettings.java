package com.mytechnology.video.vgplayer.extras;

import android.app.UiModeManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.utility.CommonFunctions;

public class AppSettings extends AppCompatActivity {

    private static final String TAG = "AppSettingActivity";
    public static boolean themeDark;
    public static boolean themeLight;
    public static boolean themeDefault;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonFunctions.setTheme(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preferences = getSharedPreferences(getPackageName() + "Change Theme", MODE_PRIVATE);
        editor = preferences.edit();
        themeDark = preferences.getBoolean("Theme Dark", false);
        themeLight = preferences.getBoolean("Theme Light", false);
        themeDefault = preferences.getBoolean("Theme Default", true);

        RadioButton radioButton_light = findViewById(R.id.radioButton_light);
        RadioButton radioButton_dark = findViewById(R.id.radioButton_dark);
        RadioButton radioButton_default = findViewById(R.id.radioButton_default);
        radioButton_light.setChecked(themeLight);
        radioButton_dark.setChecked(themeDark);
        radioButton_default.setChecked(themeDefault);

        RadioGroup radioGroup = findViewById(R.id.radioGroup_theme);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButton_light) {
                // Apply light theme
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                    UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
                    Log.d(TAG, "onCheckedChanged: " + uiModeManager.getCurrentModeType());
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                editor.putBoolean("Theme Light", true);
                editor.putBoolean("Theme Dark", false);
                editor.putBoolean("Theme Default", false);
                editor.apply();
            } else if (checkedId == R.id.radioButton_dark) {
                // Apply dark theme
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                    UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
                    Log.d(TAG, "onCheckedChanged: " + uiModeManager.getCurrentModeType());
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                editor.putBoolean("Theme Dark", true);
                editor.putBoolean("Theme Light", false);
                editor.putBoolean("Theme Default", false);
                editor.apply();
            } else if (checkedId == R.id.radioButton_default) {
                // Apply default theme
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                    UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
                    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO);
                    Log.d(TAG, "onCheckedChanged: " + uiModeManager.getCurrentModeType());
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
                editor.putBoolean("Theme Default", true);
                editor.putBoolean("Theme Dark", false);
                editor.putBoolean("Theme Light", false);
                editor.apply();
            }
        });

    }

}