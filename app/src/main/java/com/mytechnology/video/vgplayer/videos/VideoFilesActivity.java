package com.mytechnology.video.vgplayer.videos;

import static com.mytechnology.video.vgplayer.utility.CommonFunctions.getVideosWithSort;

import android.app.ComponentCaller;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
import com.mytechnology.video.vgplayer.utility.SwipeToShareCallback;

import java.util.ArrayList;
import java.util.Objects;

public class VideoFilesActivity extends AppCompatActivity implements VideoFilesAdapter.ItemClickListener {
    public static ArrayList<VideoModel> videoModels;
    VideoFilesAdapter adapter;
    ActivityVideoFilesBinding binding;
    SearchView searchView;
    String myVFolder;
    RecyclerView recyclerView;
    private final Object lock = new Object();

    static {
        VideoFilesActivity.videoModels = new ArrayList<>();
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
        searchView = binding.searchViewVideoFiles;
        recyclerView = binding.videoFilesRV;
        myVFolder = getIntent().getStringExtra("Folder Name");
        ((ActionBar) Objects.requireNonNull((Object) getSupportActionBar())).setTitle(myVFolder);

        videoModels = getVideosWithSort(getApplicationContext(), myVFolder);
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

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToShareCallback(VideoFilesActivity.this, lock));
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, @NonNull ComponentCaller caller) {
        super.onActivityResult(requestCode, resultCode, data, caller);
        if (requestCode == 222333) {
            startActivity(getIntent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.videofiles_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences preferences = getSharedPreferences("com.mytechnology.video.vgplayer.sort_Video", MODE_PRIVATE);
        if (item.getItemId() == R.id.mainMenu_search) {
            searchView.setVisibility(View.VISIBLE);
            item.setVisible(false);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText != null) {
                        adapter.filter(newText);
                        return true;
                    }
                    return false;
                }
            });
            searchView.setOnCloseListener(() -> {
                item.setVisible(true);
                searchView.setVisibility(View.GONE);
                return true;
            });

        } else if (item.getItemId() == R.id.menu_sort_by_name) {
            preferences.edit().putString("AUDIO_SORT", "byName").apply();
            startActivity(getIntent());
        } else if (item.getItemId() == R.id.menu_sort_by_date) {
            preferences.edit().putString("AUDIO_SORT", "byDate").apply();
            startActivity(getIntent());
        } else if (item.getItemId() == R.id.menu_sort_by_size) {
            preferences.edit().putString("AUDIO_SORT", "bySize").apply();
            startActivity(getIntent());
        } else if (item.getItemId() == R.id.menu_sort_by_duration) {
            preferences.edit().putString("AUDIO_SORT", "byDuration").apply();
            startActivity(getIntent());
        }
        return super.onOptionsItemSelected(item);
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
