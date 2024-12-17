package com.mytechnology.video.vgplayer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mytechnology.video.vgplayer.databinding.ActivityMainBinding;
import com.mytechnology.video.vgplayer.videos.VideoFolderAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    VideoFolderAdapter adapter;
    ActivityMainBinding binding;
    RecyclerView recyclerView;
    ArrayList<String> videoFolderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        final ActivityMainBinding inflate = ActivityMainBinding.inflate(getLayoutInflater());
       binding = inflate;
        final ConstraintLayout root = inflate.getRoot();
        setContentView(root);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        videoFolderList = getVideoFolder(this);
        (recyclerView = binding.videoFolderRV).setLayoutManager(new LinearLayoutManager(this));
        final VideoFolderAdapter videoFolderAdapter = new VideoFolderAdapter(this, videoFolderList);
        adapter = videoFolderAdapter;
        recyclerView.setAdapter(videoFolderAdapter);


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
}