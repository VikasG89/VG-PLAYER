package com.mytechnology.video.vgplayer.videos;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Locale;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.VideoFilesViewHolder> {
    Context context;
    ArrayList<VideoModel> videoModels;
    final ItemClickListener clickListener;

    public VideoFilesAdapter(final Context context, final ArrayList<VideoModel> videoModels, ItemClickListener clickListener) {
        this.context = context;
        this.videoModels = videoModels;
        this.clickListener = clickListener;
    }

    public void onBindViewHolder(final VideoFilesViewHolder videoFilesViewHolder, final int position) {
        ((RequestBuilder<?>) Glide.with(context).load(videoModels.get(position).getPath()).centerCrop()).into(videoFilesViewHolder.thumbnail);
        videoFilesViewHolder.fileName.setText(videoModels.get(position).getName());
        videoFilesViewHolder.duration.setText(ConvertSecondToHHMMSSString(videoModels.get(position).getDuration()));
        SharedPreferences preferences = context.getSharedPreferences("video_player", MODE_PRIVATE);
        if (preferences != null && !preferences.contains(videoModels.get(position).getName())) {
            videoFilesViewHolder.isNew.setVisibility(View.VISIBLE);
        }
        videoFilesViewHolder.layout.setOnClickListener(v ->{
            clickListener.onItemClick(videoFilesViewHolder.getBindingAdapterPosition());
        });

    }

    public int getItemCount() {
        return videoModels.size();
    }

    public interface ItemClickListener {
        void onItemClick(final int adapterPotion);
    }

    private String ConvertSecondToHHMMSSString(int nSecondTime) {
        return LocalTime.MIN.plusSeconds(Math.abs(nSecondTime/1000)).toString();
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
        TextView duration, isNew;

        public VideoFilesViewHolder(final View view) {
            super(view);
            final LayoutRvFilesBinding bind = LayoutRvFilesBinding.bind(view);
            binding = bind;
            fileName = bind.fileTitle;
            thumbnail = binding.thumbnail;
            layout = binding.filesLayoutRow;
            duration = binding.duration;
            isNew = binding.textViewNew;
        }
    }
}
