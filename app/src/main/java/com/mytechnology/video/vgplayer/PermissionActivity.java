package com.mytechnology.video.vgplayer;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.Arrays;

public class PermissionActivity extends AppCompatActivity {
    private static String[] PERMISSIONS_STORAGE;
    Intent intent;
    int permission;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        //Hide Status Bar
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        insetsController.hide(WindowInsetsCompat.Type.systemBars());


        if (Build.VERSION.SDK_INT >= 33) {
            PERMISSIONS_STORAGE = new String[]{"android.permission.READ_MEDIA_VIDEO"};
        } else {
            PERMISSIONS_STORAGE = new String[]{"android.permission.READ_EXTERNAL_STORAGE"};
        }
        intent = new Intent(this, MainActivity.class);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                getPermission();
            } catch (final Exception ex) {
                Log.e("PermissionActivity", "onCreate: " + ex.getMessage());
            }
        }).start();

    }

    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    permission = PackageManager.PERMISSION_GRANTED;
                    startActivity(intent);
                    finish();
                }

            }
        } else {
            Toast.makeText(this, "Permissions Denied!!\nAll Permissions Are Required", Toast.LENGTH_LONG).show();
            getPermission();
        }
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Arrays.toString(PermissionActivity.PERMISSIONS_STORAGE)) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PermissionActivity.PERMISSIONS_STORAGE, 1);
        } else {
            startActivity(intent);
            finish();
        }

    }

}
