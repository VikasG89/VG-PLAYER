package com.mytechnology.video.vgplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.mytechnology.video.vgplayer.databinding.ActivityMainBinding;
import com.mytechnology.video.vgplayer.extras.AppSettings;
import com.mytechnology.video.vgplayer.extras.ReviewActivity;
import com.mytechnology.video.vgplayer.videos.VideoFolderAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        final ActivityMainBinding inflate = ActivityMainBinding.inflate(getLayoutInflater());
        final ConstraintLayout root = inflate.getRoot();
        setContentView(root);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ArrayList<String> videoFolderList = getVideoFolder(this);
        RecyclerView recyclerView;
        (recyclerView = inflate.videoFolderRV).setLayoutManager(new LinearLayoutManager(this));
        final VideoFolderAdapter videoFolderAdapter = new VideoFolderAdapter(this, videoFolderList);
        recyclerView.setAdapter(videoFolderAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainactivity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mainMenu_setting) {
            Intent intent = new Intent(this, AppSettings.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == R.id.mainMenu_share) {
            Toast.makeText(this, "Share App Link pressed", Toast.LENGTH_SHORT).show();
            /*

            Todo Stuff for App Link Share

             */
        } else if (item.getItemId() == R.id.mainMenu_rateUs) {
            Intent intent = new Intent(this, ReviewActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == R.id.mainMenu_about) {
            Toast.makeText(this, "About pressed", Toast.LENGTH_SHORT).show();
            /*

            Todo Stuff for About Us

             */
        } else if (item.getItemId() == R.id.mainMenu_licences) {
            showLicenses();
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<String> getVideoFolder(final Context context) {
        final ArrayList<String> list = new ArrayList<>();
        Uri uri;
        if (Build.VERSION.SDK_INT >= 29) {
            uri = MediaStore.Video.Media.getContentUri("external");
        } else {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        final Cursor query = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, "title");
        if (query != null) {
            final int columnIndexOrThrow = query.getColumnIndexOrThrow("_data");
            while (query.moveToNext()) {
                final String string = query.getString(columnIndexOrThrow);
                final int lastIndex = string.lastIndexOf("/");
                final String substring = string.substring(0, lastIndex);
                final String substring2 = substring.substring(substring.lastIndexOf("/") + 1, lastIndex);
                if (!list.contains(substring2))
                    list.add(substring2);
            }
        }
        assert query != null;
        query.close();
        return list;
    }

    private void showLicenses() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.exit_to_app)
                .setTitle("View Licenses?")
                .setMessage("You want to view Licenses?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    OssLicensesMenuActivity.setActivityTitle("Licenses");
                    // Handle the case where the user grants the permission
                    Intent intent = new Intent(this, OssLicensesMenuActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}