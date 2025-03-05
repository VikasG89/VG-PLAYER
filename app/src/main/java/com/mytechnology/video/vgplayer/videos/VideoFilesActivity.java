package com.mytechnology.video.vgplayer.videos;

import static android.content.ContentValues.TAG;
import static com.mytechnology.video.vgplayer.utility.CommonFunctions.STORAGE_PERMISSION_CODE;
import static com.mytechnology.video.vgplayer.utility.CommonFunctions.checkStoragePermissions;
import static com.mytechnology.video.vgplayer.utility.CommonFunctions.getVideosWithSort;
import static com.mytechnology.video.vgplayer.utility.CommonFunctions.requestForStoragePermissions;
import static com.mytechnology.video.vgplayer.videos.VideoPlayActivity.MY_SHARED_PREFS_VIDEO;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
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
import com.mytechnology.video.vgplayer.utility.CommonFunctions;
import com.mytechnology.video.vgplayer.utility.FileUpdater;
import com.mytechnology.video.vgplayer.utility.ShareHelper;
import com.mytechnology.video.vgplayer.utility.SwipeToShareCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class VideoFilesActivity extends AppCompatActivity implements VideoFilesAdapter.ItemClickListener {
    public static ArrayList<VideoModel> videoModels;
    private VideoFilesAdapter adapter;
    protected ActivityVideoFilesBinding binding;
    private String myVFolder;
    private final Object lock = new Object();
    static ActionMode actionMode = null;
    private ActionMode.Callback actionModeCallback;
    private ActivityResultLauncher<Intent> storageActivityResultLauncher;
    private boolean permissionGranted = false;

    boolean deleted;

    static {
        VideoFilesActivity.videoModels = new ArrayList<>();
    }

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        CommonFunctions.setTheme(this);
        EdgeToEdge.enable(this);
        ActivityVideoFilesBinding inflate = ActivityVideoFilesBinding.inflate(getLayoutInflater());
        binding = inflate;
        setContentView(inflate.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RecyclerView recyclerView = binding.videoFilesRV;
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
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    if (menu.findItem(R.id.multi_menu_delete) != null) {
                        menu.findItem(R.id.multi_menu_delete).setVisible(true);
                    }
                }
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
                            .setPositiveButton("Yes", (_, _) -> {
                                if (!checkStoragePermissions(VideoFilesActivity.this)) {
                                    requestForStoragePermissions(VideoFilesActivity.this, storageActivityResultLauncher);
                                } else {
                                    for (int i = 0; i < adapter.selectedVideoModels.size(); i++) {
                                        deleted = FileUpdater.deleteVideoFile(VideoFilesActivity.this, adapter.selectedVideoModels.get(i).getPath());
                                    }
                                    if (deleted) {
                                        // File deleted successfully
                                        Toast.makeText(VideoFilesActivity.this, "Video Deleted Successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // File deletion failed
                                        Toast.makeText(VideoFilesActivity.this, "Error Deleting File!\n Please try again!!", Toast.LENGTH_SHORT).show();
                                    }
                                    actionMode = null;
                                    recreate();
                                }
                            })
                            .setNegativeButton("No", (dialog, _) -> {
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

        storageActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), _ -> {
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
                .setPositiveButton("Yes", (_, _) -> {
                    permissionGranted = checkStoragePermissions(VideoFilesActivity.this);
                    if (!permissionGranted) {
                        requestForStoragePermissions(VideoFilesActivity.this, storageActivityResultLauncher);
                    } else {
                        boolean deleted = FileUpdater.deleteVideoFile(this, videoModels.get(position).getPath());
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
                .setNegativeButton("No", (dialog, _) -> {
                    // User cancelled the deletion
                    dialog.dismiss();
                });
        builder.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void reNameFile(int position) {
        permissionGranted = checkStoragePermissions(VideoFilesActivity.this);
        if (!permissionGranted) {
            requestForStoragePermissions(VideoFilesActivity.this, storageActivityResultLauncher);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Rename Video?")
                    .setIcon(R.drawable.rename_icon)
                    .setMessage("Are you sure you want to rename this video?");
            EditText edtRename = new EditText(this);
            File file = new File(videoModels.get(position).getPath());
            String fileName = file.getName();
            String title = fileName.substring(0, fileName.lastIndexOf("."));
            String extension = fileName.substring(fileName.lastIndexOf("."));
            edtRename.setText(title);
            builder.setView(edtRename);
            edtRename.requestFocus();
            builder.setPositiveButton("Yes", (_, _) -> {
                String newFileName = edtRename.getText().toString() + extension;
                try {
                    boolean isRenamed = FileUpdater.renameFile(this, file.getAbsolutePath(), newFileName);
                    if (isRenamed) {
                        Log.d("FileRename", "File renamed successfully");
                        SharedPreferences preferences = getSharedPreferences(MY_SHARED_PREFS_VIDEO, MODE_PRIVATE);
                        preferences.edit().remove(videoModels.get(position).getPath()).apply();
                        adapter.notifyDataSetChanged();
                        SystemClock.sleep(200);
                        startActivity(getIntent());
                    } else {
                        Log.d("FileRename", "File rename failed");
                        Toast.makeText(this, "Error Renaming File! Please try again!!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.d("FileRename", "Exception occurred: " + e.getMessage());
                    Toast.makeText(this, "Exception occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("No", (dialog, _) -> dialog.dismiss());
            builder.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                //check each permission if granted or not
                permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                //External Storage permissions granted
                Log.d(TAG, "onRequestPermissionsResult: External Storage permissions granted");
                Toast.makeText(this, "External Storage permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                //External Storage permission denied
                Log.d(TAG, "onRequestPermissionsResult: External Storage permission denied");
                Toast.makeText(this, "External Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
