package com.mytechnology.video.vgplayer.utility;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.UI_MODE_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mytechnology.video.vgplayer.videos.VideoModel;

import java.util.ArrayList;
import java.util.Locale;

public class CommonFunctions {

    public static final int STORAGE_PERMISSION_CODE = 321;

    public static String ConvertSecondToHHMMSSString(int nSecondTime) {
        int nSecond = nSecondTime / 1000;
        int hrs = nSecond / 3600;
        int min = (nSecond % 3600) / 60;
        int sec = nSecond % 60;
        if (hrs == 0) {
            return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, min, sec);
        }
    }

    public static ArrayList<VideoModel> getVideos(final Context context, String folderName) {
        final ArrayList<VideoModel> list = new ArrayList<>();
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = MediaStore.Video.Media.getContentUri("external");
        } else {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        final Cursor query;
        if (folderName != null) {
            folderName = "%" + folderName + "%";
            query = context.getContentResolver().query(uri, new String[]{"_data", "_display_name", "date_added", "duration", "_size"},
                    "_data like?", new String[]{folderName}, null);
        } else {
            query = context.getContentResolver().query(uri, new String[]{"_data", "_display_name", "date_added", "duration", "_size"},
                    null, null, null);
        }

        if (query != null) {
            final int columnIndexOrThrow = query.getColumnIndexOrThrow("_data");
            final int columnIndexOrThrow2 = query.getColumnIndexOrThrow("_display_name");
            final int columnIndexOrThrow3 = query.getColumnIndexOrThrow("date_added");
            final int columnIndexOrThrow4 = query.getColumnIndexOrThrow("duration");
            final int columnIndexOrThrow5 = query.getColumnIndexOrThrow("_size");
            while (query.moveToNext()) {
                String path = query.getString(columnIndexOrThrow);
                if (columnIndexOrThrow5 > 1)
                    list.add(new VideoModel(path, query.getString(columnIndexOrThrow2), query.getString(columnIndexOrThrow3),
                            query.getInt(columnIndexOrThrow4), query.getInt(columnIndexOrThrow5)));
            }
        }
        assert query != null;
        query.close();
        return list;
    }

    public static ArrayList<VideoModel> getVideosWithSort(final Context context, String folderName) {
        final ArrayList<VideoModel> list = new ArrayList<>();
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = MediaStore.Video.Media.getContentUri("external");
        } else {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        folderName = "%" + folderName + "%";
        final String string = context.getSharedPreferences("com.mytechnology.video.vgplayer.sort_Video", MODE_PRIVATE)
                .getString("AUDIO_SORT", "abcd");
        String sortOrder = switch (string) {
            case "byName" -> "_display_name ASC";
            case "byDate" -> "date_added DESC";
            case "bySize" -> "_size DESC";
            case "byDuration" -> "duration DESC";
            default -> "";
        };
        final Cursor query = context.getContentResolver().query(uri, new String[]{"_data", "_display_name", "date_added", "duration", "_size"},
                "_data like?", new String[]{folderName}, sortOrder);
        if (query != null) {
            final int columnIndexOrThrow = query.getColumnIndexOrThrow("_data");
            final int columnIndexOrThrow2 = query.getColumnIndexOrThrow("_display_name");
            final int columnIndexOrThrow3 = query.getColumnIndexOrThrow("date_added");
            final int columnIndexOrThrow4 = query.getColumnIndexOrThrow("duration");
            final int columnIndexOrThrow5 = query.getColumnIndexOrThrow("_size");
            while (query.moveToNext()) {
                String path = query.getString(columnIndexOrThrow);
                if (columnIndexOrThrow5 > 1) {
                    list.add(new VideoModel(path, query.getString(columnIndexOrThrow2), query.getString(columnIndexOrThrow3),
                            query.getInt(columnIndexOrThrow4), query.getInt(columnIndexOrThrow5)));
                }
            }
        }
        assert query != null;
        query.close();
        return list;
    }

    public static boolean checkStoragePermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        } else {
            //Below android 11
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestForStoragePermissions(Context context, ActivityResultLauncher<Intent> storageActivityResultLauncher) {
        //Android is 10 (Q) or below
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }
    }

    public static void setTheme(Context context) {
        boolean themeDark;
        boolean themeLight;
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + "Change Theme", MODE_PRIVATE);
        themeDark = preferences.getBoolean("Theme Dark", false);
        themeLight = preferences.getBoolean("Theme Light", false);
        if (themeDark) {
            // Apply dark theme
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        } else if (themeLight) {
            // Apply light theme
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        } else {
            // Apply default theme
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
                UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        }
    }

}
