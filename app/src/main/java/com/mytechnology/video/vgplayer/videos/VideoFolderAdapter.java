package com.mytechnology.video.vgplayer.videos;

import static android.content.Context.MODE_PRIVATE;
import static com.mytechnology.video.vgplayer.utility.CommonFunctions.getVideos;
import static com.mytechnology.video.vgplayer.videos.VideoPlayActivity.MY_SHARED_PREFS_VIDEO;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.cardview.widget.CardView;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.LayoutRvFolderBinding;

import java.util.ArrayList;

public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.VideoFolderViewHolder> {
    Context context;
    ArrayList<String> videoFolderList;
    ArrayList<VideoModel> fileNameArray = new ArrayList<>();

    public VideoFolderAdapter(final Context context, final ArrayList<String> videoFolderList) {
        this.context = context;
        this.videoFolderList = videoFolderList;
    }

    @NonNull
    public VideoFolderViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int n) {
        return new VideoFolderViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_rv_folder, viewGroup, false));
    }

    @OptIn(markerClass = UnstableApi.class)
    public void onBindViewHolder(final VideoFolderViewHolder videoFolderViewHolder, final int position) {
        videoFolderViewHolder.fileName.setText(videoFolderList.get(position));
        fileNameArray = getVideos(context, videoFolderList.get(position));
        SharedPreferences preferences = context.getSharedPreferences(MY_SHARED_PREFS_VIDEO, MODE_PRIVATE);
        for (VideoModel model : fileNameArray) {
            if (preferences!= null && !preferences.contains(model.getPath())) {
                videoFolderViewHolder.isNewVideoAvailable.setVisibility(View.VISIBLE);
                break;
            }
        }
        videoFolderViewHolder.layout.setOnClickListener(view -> {
            final Intent intent = new Intent(context, VideoFilesActivity.class);
            intent.putExtra("Folder Name", videoFolderList.get(videoFolderViewHolder.getBindingAdapterPosition()));
            context.startActivity(intent);
        });
    }

    public int getItemCount() {
        return videoFolderList.size();
    }

    public static class VideoFolderViewHolder extends RecyclerView.ViewHolder {
        private final TextView fileName;
        private final CardView layout;
        private final TextView isNewVideoAvailable;

        public VideoFolderViewHolder(final View view) {
            super(view);
            LayoutRvFolderBinding vFolderBinding = LayoutRvFolderBinding.bind(view);
            fileName = vFolderBinding.fileTitle;
            layout = vFolderBinding.folderLayout;
            isNewVideoAvailable = vFolderBinding.textViewNewFolder;
        }
    }
}
