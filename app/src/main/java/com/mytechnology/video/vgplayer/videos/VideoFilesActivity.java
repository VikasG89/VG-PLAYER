package com.mytechnology.video.vgplayer.videos;

import android.app.ComponentCaller;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytechnology.video.vgplayer.MainActivity;
import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.ActivityVideoFilesBinding;
import com.mytechnology.video.vgplayer.utility.ShareHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class VideoFilesActivity extends AppCompatActivity implements VideoFilesAdapter.ItemClickListener {
    static ArrayList<VideoModel> videoModels;
    VideoFilesAdapter adapter;
    ActivityVideoFilesBinding binding;
    String myVFolder;
    RecyclerView recyclerView;
    String sortOrder;
    ShareHelper shareHelper;

    static {
        VideoFilesActivity.videoModels = new ArrayList<>();
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        EdgeToEdge.enable(this);
        ActivityVideoFilesBinding inflate = ActivityVideoFilesBinding.inflate(getLayoutInflater());
        binding = inflate;
        setContentView(inflate.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = binding.videoFilesRV;
        myVFolder = getIntent().getStringExtra("Folder Name");
        ((ActionBar) Objects.requireNonNull((Object) getSupportActionBar())).setTitle(myVFolder);

        shareHelper = new ShareHelper(VideoFilesActivity.this);

        VideoFilesActivity.videoModels = getVideos(getApplicationContext(), myVFolder);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final VideoFilesAdapter videoFilesAdapter = new VideoFilesAdapter(this, VideoFilesActivity.videoModels, this);
        adapter = videoFilesAdapter;
        recyclerView.setAdapter(videoFilesAdapter);
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(VideoFilesActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.RIGHT) {
                try {
                    shareHelper.shareVideo(VideoFilesActivity.videoModels.get(viewHolder.getBindingAdapterPosition()).getPath());
                } catch (Exception e) {
                    Toast.makeText(VideoFilesActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }


        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, @NonNull ComponentCaller caller) {
        super.onActivityResult(requestCode, resultCode, data, caller);
        if (requestCode == 222333) {
            recreate();
        }
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

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onItemClick(int adapterPotion) {
        Intent intent = new Intent(this, VideoPlayActivity.class);
        intent.putExtra("position", adapterPotion);
        intent.putExtra("Parcelable", videoModels);
        intent.putExtra("Folder Name", myVFolder);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
