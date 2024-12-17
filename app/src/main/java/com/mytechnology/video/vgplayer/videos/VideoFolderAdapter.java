package com.mytechnology.video.vgplayer.videos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.LayoutRvFolderBinding;

import java.util.ArrayList;

public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.VideoFolderViewHolder> {
    Context context;
    ArrayList<String> videoFolderList;

    public VideoFolderAdapter(final Context context, final ArrayList<String> videoFolderList) {
        this.context = context;
        this.videoFolderList = videoFolderList;
    }

    public int getItemCount() {
        return videoFolderList.size();
    }

    public void onBindViewHolder(final VideoFolderViewHolder videoFolderViewHolder, final int n) {
        videoFolderViewHolder.fileName.setText(videoFolderList.get(n));
        videoFolderViewHolder.layout.setOnClickListener(view -> {
            final Intent intent = new Intent(context, VideoFilesActivity.class);
            intent.putExtra("Folder Name", videoFolderList.get(videoFolderViewHolder.getBindingAdapterPosition()));
            context.startActivity(intent);
        });
    }

    @NonNull
    public VideoFolderViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int n) {
        return new VideoFolderViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_rv_folder, viewGroup, false));
    }

    public static class VideoFolderViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        CardView layout;
        LayoutRvFolderBinding vFolderBinding;

        public VideoFolderViewHolder(final View view) {
            super(view);
            final LayoutRvFolderBinding bind = LayoutRvFolderBinding.bind(view);

            vFolderBinding = bind;

            fileName = bind.fileTitle;

            layout = vFolderBinding.folderLayout;
        }
    }
}
