package com.mytechnology.video.vgplayer.videos;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.ActivityVideoFilesBinding;

import java.util.ArrayList;
import java.util.Objects;

public class VideoFilesActivity extends AppCompatActivity {
    static ArrayList<VideoModel> videoModels;
    VideoFilesAdapter adapter;
    ActivityVideoFilesBinding binding;
    String myVFolder;
    RecyclerView recyclerView;
    String sortOrder;

    static {
        VideoFilesActivity.videoModels = new ArrayList<>();
    }

    private ArrayList<VideoModel> getVideos(final Context context, String s) {
        final ArrayList<VideoModel> list = new ArrayList<>();
        Uri uri;
        if (Build.VERSION.SDK_INT >= 29) {
            uri = MediaStore.Video.Media.getContentUri("external");
        } else {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        s = "%" + s + "%";
        final String string = context.getSharedPreferences("com.mytechnology.fragments.ui.videos", 0)
                .getString("AUDIO_SORT", "abcd");
        if (string.equals("byName")) {
            sortOrder = "_display_name ASC";
        } else if (string.equals("byDate")) {
            sortOrder = "date_added DESC";
        } else if (string.equals("byArtist")) {
            sortOrder = "_size DESC";
        }
        final Cursor query = context.getContentResolver().query(uri, new String[]{"_data", "_display_name", "date_added", "duration", "_size"},
                "_data like?", new String[]{s}, sortOrder);
        if (query != null) {
            final int columnIndexOrThrow = query.getColumnIndexOrThrow("_data");
            final int columnIndexOrThrow2 = query.getColumnIndexOrThrow("_display_name");
            final int columnIndexOrThrow3 = query.getColumnIndexOrThrow("date_added");
            final int columnIndexOrThrow4 = query.getColumnIndexOrThrow("duration");
            final int columnIndexOrThrow5 = query.getColumnIndexOrThrow("_size");
            while (query.moveToNext()) {
                s = query.getString(columnIndexOrThrow);
                list.add(new VideoModel(s, query.getString(columnIndexOrThrow2), query.getString(columnIndexOrThrow3),
                        query.getInt(columnIndexOrThrow4), query.getInt(columnIndexOrThrow5)));
            }
        }
        assert query != null;
        query.close();
        return list;
    }

    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        ((ActionBar) Objects.requireNonNull((Object) getSupportActionBar())).setTitle(R.string.title_video);
        ActivityVideoFilesBinding inflate = ActivityVideoFilesBinding.inflate(getLayoutInflater());
        binding = inflate;
        setContentView(inflate.getRoot());
        recyclerView = binding.videoFilesRV;
        myVFolder = getIntent().getStringExtra("Folder Name");
        VideoFilesActivity.videoModels = getVideos(getApplicationContext(), myVFolder);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final VideoFilesAdapter videoFilesAdapter = new VideoFilesAdapter(this, VideoFilesActivity.videoModels);
        adapter = videoFilesAdapter;
        recyclerView.setAdapter(videoFilesAdapter);
    }
}
