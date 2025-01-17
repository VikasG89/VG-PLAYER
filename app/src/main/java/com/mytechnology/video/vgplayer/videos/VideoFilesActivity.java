package com.mytechnology.video.vgplayer.videos;

import static android.content.ContentValues.TAG;
import static com.mytechnology.video.vgplayer.utility.CommonFunctions.getVideosWithSort;
import static com.mytechnology.video.vgplayer.videos.VideoPlayActivity.MY_SHARED_PREFS_VIDEO;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.mytechnology.video.vgplayer.utility.SwipeToShareCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class VideoFilesActivity extends AppCompatActivity implements VideoFilesAdapter.ItemClickListener {
    public static ArrayList<VideoModel> videoModels;
    VideoFilesAdapter adapter;
    ActivityVideoFilesBinding binding;
    String myVFolder;
    RecyclerView recyclerView;
    private final Object lock = new Object();
    private static final int STORAGE_PERMISSION_CODE = 321;
    boolean permissionGrantForSdk33;
    static ActionMode actionMode = null;
    ActionMode.Callback actionModeCallback;

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
        recyclerView = binding.videoFilesRV;
        myVFolder = getIntent().getStringExtra("Folder Name");
        ((ActionBar) Objects.requireNonNull((Object) getSupportActionBar())).setTitle(myVFolder);

        videoModels = getVideosWithSort(getApplicationContext(), myVFolder);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoFilesAdapter(this, VideoFilesActivity.videoModels, this);
        recyclerView.setAdapter(adapter);

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

        actionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.multi_select_item_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.multi_menu_share) {
                    ArrayList<String> filePaths = new ArrayList<>();
                    for (VideoModel videoModel : adapter.selectedVideoModels) {
                        filePaths.add(videoModel.getPath());
                    }
                    ShareHelper shareHelper = new ShareHelper(VideoFilesActivity.this);
                    shareHelper.shareMultiVideos(filePaths);

                } else if (item.getItemId() == R.id.multi_menu_delete) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(VideoFilesActivity.this);
                    String massage;
                    if (adapter.selectedVideoModels.size() <= 1) {
                        massage = adapter.selectedVideoModels.size() + " Video Selected.\n\n" + " Are you sure delete this video?";
                    } else {
                        massage = adapter.selectedVideoModels.size() + " Videos Selected.\n\n" + " Are you sure delete these videos?";
                    }
                    builder.setTitle("Delete Video?")
                            .setIcon(R.drawable.delete_forever_icon)
                            .setMessage(massage)
                            .setPositiveButton("Yes", (dialog, id) -> {
                                permissionGrantForSdk33 = checkStoragePermissions();
                                if (!permissionGrantForSdk33) {
                                    requestForStoragePermissions();
                                } else {
                                    for (int i = 0; i < adapter.selectedVideoModels.size(); i++) {
                                        File file = new File(adapter.selectedVideoModels.get(i).getPath());
                                        boolean deleted = file.delete();
                                        if (deleted) {
                                            // File deleted successfully
                                            Toast.makeText(VideoFilesActivity.this, "Video Deleted Successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // File deletion failed
                                            Toast.makeText(VideoFilesActivity.this, "Error Deleting File!\n Please try again!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    recreate();
                                }
                            })
                            .setNegativeButton("No", (dialog, id) -> {
                                // User cancelled the deletion
                                dialog.dismiss();
                            });
                    builder.show();
                }
                return true;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.multiSelection = false;
                adapter.selectedVideoModels.clear();
                adapter.notifyDataSetChanged();
                actionMode = null;
            }
        };


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
            SearchView searchView = (SearchView) item.getActionView();
            assert searchView != null;
            searchView.setQueryHint("Type to Search video");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.filter(newText);
                    return true;
                }
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

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void longClick(int position, VideoFilesAdapter.VideoFilesViewHolder viewHolder) {
        if (actionMode != null) {
            return;
        } else {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        adapter.multiSelection = true;
        viewHolder.checkBox.setVisibility(View.VISIBLE);
        viewHolder.filesMenu.setVisibility(View.GONE);
        if (adapter.selectedVideoModels.contains(videoModels.get(position))) {
            adapter.selectedVideoModels.remove(videoModels.get(position));
            viewHolder.checkBox.setChecked(false);
        } else {
            adapter.selectedVideoModels.add(videoModels.get(position));
            viewHolder.checkBox.setChecked(true);
        }
        runOnUiThread(() -> {
            if (actionMode != null) {
                Log.d(TAG, "Update Title: " + adapter.selectedVideoModels.size());
                actionMode.setTitle(adapter.selectedVideoModels.size() + " video selected");
            }
        });
        adapter.notifyItemChanged(position);
        adapter.notifyDataSetChanged();
    }
    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onItemClick(int position, VideoFilesAdapter.VideoFilesViewHolder viewHolder) {
        Intent intent = new Intent(this, VideoPlayActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("Parcelable", videoModels);
        intent.putExtra("Folder Name", myVFolder);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void deleteFile(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Video?")
                .setIcon(R.drawable.delete_forever_icon)
                .setMessage("Are you sure you want to delete this video?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    permissionGrantForSdk33 = checkStoragePermissions();
                    if (!permissionGrantForSdk33) {
                        requestForStoragePermissions();
                    } else {
                        File file = new File(videoModels.get(position).getPath());
                        boolean deleted = file.delete();
                        if (deleted) {
                            // File deleted successfully
                            videoModels.remove(position);
                            adapter.notifyItemRemoved(position);
                            adapter.notifyItemRangeChanged(position, videoModels.size());
                        } else {
                            // File deletion failed
                            Toast.makeText(this, "Error Deleting File!\n Please try again!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // User cancelled the deletion
                    dialog.dismiss();
                });
        builder.show();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void reNameFile(int position) {
        permissionGrantForSdk33 = checkStoragePermissions();
        if (!permissionGrantForSdk33) {
            requestForStoragePermissions();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Rename Video?")
                    .setIcon(R.drawable.rename_icon)
                    .setMessage("Are you sure you want to rename this video?");
            EditText edtRename = new EditText(this);
            File file = new File(videoModels.get(position).getPath());
            String fileName = file.getName();
            edtRename.setText(fileName);
            builder.setView(edtRename);
            edtRename.requestFocus();
            builder.setPositiveButton("Yes", (dialog, id) -> {
                String newFileName = edtRename.getText().toString();
                boolean isRenamed = file.renameTo(new File(file.getParentFile(), newFileName));
                if (isRenamed) {
                    SharedPreferences preferences = getSharedPreferences(MY_SHARED_PREFS_VIDEO, MODE_PRIVATE);
                    preferences.edit().remove(videoModels.get(position).getPath()).apply();
                    adapter.notifyItemChanged(position);
                    recreate();
                } else {
                    Toast.makeText(this, "Error Renaming File! Please try again!!", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());
            builder.show();
        }
    }

    public boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        } else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        } else {
            //Below android 11
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,}, STORAGE_PERMISSION_CODE);
        }

    }

    private final ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    //Android is 11 (R) or above
                    if (Environment.isExternalStorageManager()) {
                        //Manage External Storage Permissions Granted
                        Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
                    } else {
                        Toast.makeText(VideoFilesActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            });


}
