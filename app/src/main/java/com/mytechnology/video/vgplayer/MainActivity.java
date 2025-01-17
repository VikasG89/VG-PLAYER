package com.mytechnology.video.vgplayer;

import static android.content.ContentValues.TAG;
import static com.mytechnology.video.vgplayer.utility.CommonFunctions.checkStoragePermissions;
import static com.mytechnology.video.vgplayer.utility.CommonFunctions.getVideos;
import static com.mytechnology.video.vgplayer.utility.CommonFunctions.requestForStoragePermissions;
import static com.mytechnology.video.vgplayer.videos.VideoPlayActivity.MY_SHARED_PREFS_VIDEO;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.mytechnology.video.vgplayer.databinding.ActivityMainBinding;
import com.mytechnology.video.vgplayer.extras.AppSettings;
import com.mytechnology.video.vgplayer.extras.MainActivityAdapter;
import com.mytechnology.video.vgplayer.extras.ReviewActivity;
import com.mytechnology.video.vgplayer.videos.VideoFolderAdapter;
import com.mytechnology.video.vgplayer.videos.VideoModel;
import com.mytechnology.video.vgplayer.videos.VideoPlayActivity;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainActivityAdapter.ItemClickListener {

    RecyclerView recyclerView;
    ArrayList<VideoModel> videoArrayList = new ArrayList<>();
    boolean permissionGrantForSdk33;
    RecyclerView videoFilesRV;
    MainActivityAdapter videoFilesAdapter;

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

        videoFilesRV = findViewById(R.id.videoFilesRV);
        videoArrayList = getVideos(this, null);

        ArrayList<String> videoFolderList = getVideoFolder(this);

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
        if (item.getItemId() == R.id.mainMenu_search) {
            videoFilesRV.setLayoutManager(new LinearLayoutManager(this));
            videoFilesAdapter = new MainActivityAdapter(this, videoArrayList, this);
            videoFilesRV.setAdapter(videoFilesAdapter);

            // for search Videos from folder page / MainActivity
            SearchView searchView = (SearchView) item.getActionView();
            assert searchView != null;
            searchView.setQueryHint("Search Videos By Name");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    recyclerView.setVisibility(View.GONE);
                    videoFilesAdapter.filter(newText);
                    videoFilesRV.setVisibility(View.VISIBLE);
                    return true;
                }
            });
            item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                    recreate();
                    return true;
                }
            });

        } else if (item.getItemId() == R.id.mainMenu_setting) {
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

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onItemClick(int position, MainActivityAdapter.FilesViewHolder viewHolder) {
        Intent intent = new Intent(this, VideoPlayActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("Parcelable", videoArrayList);
        intent.putExtra("Folder Name", "NO Folder Name");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void deleteFile(int position) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Delete Video?")
                .setIcon(R.drawable.delete_forever_icon)
                .setMessage("Are you sure you want to delete this video?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    permissionGrantForSdk33 = checkStoragePermissions(MainActivity.this);
                    if (!permissionGrantForSdk33) {
                        requestForStoragePermissions(MainActivity.this, storageActivityResultLauncher);
                    } else {
                        File file = new File(videoArrayList.get(position).getPath());
                        boolean deleted = file.delete();
                        if (deleted) {
                            // File deleted successfully
                            videoArrayList.remove(position);
                            videoFilesAdapter.notifyItemRemoved(position);
                            videoFilesAdapter.notifyItemRangeChanged(position, videoArrayList.size());
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
        permissionGrantForSdk33 = checkStoragePermissions(MainActivity.this);
        if (!permissionGrantForSdk33) {
            requestForStoragePermissions(MainActivity.this, storageActivityResultLauncher);
        } else {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Rename Video?")
                    .setIcon(R.drawable.rename_icon)
                    .setMessage("Are you sure you want to rename this video?");
            EditText edtRename = new EditText(this);
            File file = new File(videoArrayList.get(position).getPath());
            String fileName = file.getName();
            edtRename.setText(fileName);
            builder.setView(edtRename);
            edtRename.requestFocus();
            builder.setPositiveButton("Yes", (dialog, id) -> {
                String newFileName = edtRename.getText().toString();
                boolean isRenamed = file.renameTo(new File(file.getParentFile(), newFileName));
                if (isRenamed) {
                    SharedPreferences preferences = getSharedPreferences(MY_SHARED_PREFS_VIDEO, MODE_PRIVATE);
                    preferences.edit().remove(videoArrayList.get(position).getPath()).apply();
                    videoFilesAdapter.notifyItemChanged(position);
                    recreate();
                } else {
                    Toast.makeText(this, "Error Renaming File! Please try again!!", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());
            builder.show();
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
                        Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            });
}