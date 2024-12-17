package com.mytechnology.video.vgplayer.videos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.LayoutRvFilesBinding;

import java.util.ArrayList;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.VideoFilesViewHolder> {
    Context context;
    ArrayList<VideoModel> videoModels;

    public VideoFilesAdapter(final Context context, final ArrayList<VideoModel> videoModels) {
        this.context = context;
        this.videoModels = videoModels;
    }

    public int getItemCount() {
        return videoModels.size();
    }

    public void onBindViewHolder(final VideoFilesViewHolder videoFilesViewHolder, final int n) {
        ((RequestBuilder<?>) Glide.with(context).load(videoModels.get(n).getPath()).centerCrop()).into(videoFilesViewHolder.thumbnail);
        videoFilesViewHolder.fileName.setText(videoModels.get(n).getName());
        videoFilesViewHolder.layout.setOnClickListener(v -> {
            Intent intent = new Intent(context, VideoPlayActivity.class);
            intent.putExtra("position", videoFilesViewHolder.getBindingAdapterPosition());
            intent.putExtra("Parcelable", videoModels);
            context.startActivity(intent);
        });
    }

    @NonNull
    public VideoFilesViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int n) {
        return new VideoFilesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_rv_files, viewGroup, false));
    }

    public static class VideoFilesViewHolder extends RecyclerView.ViewHolder {
        LayoutRvFilesBinding binding;
        TextView fileName;
        CardView layout;
        ImageView thumbnail;

        public VideoFilesViewHolder(final View view) {
            super(view);
            final LayoutRvFilesBinding bind = LayoutRvFilesBinding.bind(view);
            binding = bind;
            fileName = bind.fileTitle;
            thumbnail = binding.thumbnail;
            layout = binding.filesLayoutRow;
        }
    }
}
