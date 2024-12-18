package com.mytechnology.video.vgplayer.videos;

import android.content.Context;
import android.content.Intent;
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

public class VideoFilesActivity extends AppCompatActivity implements VideoFilesAdapter.ItemClickListener {
    static ArrayList<VideoModel> videoModels;
    VideoFilesAdapter adapter;
    ActivityVideoFilesBinding binding;
    String myVFolder;
    RecyclerView recyclerView;
    String sortOrder;

    static {
        VideoFilesActivity.videoModels = new ArrayList<>();
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
        final VideoFilesAdapter videoFilesAdapter = new VideoFilesAdapter(this, VideoFilesActivity.videoModels, this);
        adapter = videoFilesAdapter;
        recyclerView.setAdapter(videoFilesAdapter);
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
        final String string = context.getSharedPreferences("com.mytechnology.video.vgplayer.sort_Video", 0)
                .getString("AUDIO_SORT", "abcd");
        switch (string) {
            case "byName":
                sortOrder = "_display_name ASC";
                break;
            case "byDate":
                sortOrder = "date_added DESC";
                break;
            case "byArtist":
                sortOrder = "_size DESC";
                break;
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

    @Override
    public void onItemClick(int adapterPotion) {
            Intent intent = new Intent(this, VideoPlayActivity.class);
            intent.putExtra("position", adapterPotion);
            intent.putExtra("Parcelable", videoModels);
            intent.putExtra("Folder Name", myVFolder);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
    }
}
