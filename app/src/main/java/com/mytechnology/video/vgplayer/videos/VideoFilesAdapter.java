package com.mytechnology.video.vgplayer.videos;

import static android.content.Context.MODE_PRIVATE;
import static com.mytechnology.video.vgplayer.videos.VideoPlayActivity.MY_SHARED_PREFS_VIDEO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.LayoutRvFilesBinding;
import com.mytechnology.video.vgplayer.utility.ShareHelper;

import java.util.ArrayList;
import java.util.Locale;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.VideoFilesViewHolder> {
    Context context;
    ArrayList<VideoModel> videoModels;
    final ItemClickListener clickListener;
    ArrayList<VideoModel> videoModelFull;

    public VideoFilesAdapter(final Context context, final ArrayList<VideoModel> videoModels, ItemClickListener clickListener) {
        this.context = context;
        this.videoModels = videoModels;
        this.clickListener = clickListener;
        this.videoModelFull = new ArrayList<>(videoModels);
    }

    @OptIn(markerClass = UnstableApi.class)
    public void onBindViewHolder(final VideoFilesViewHolder viewHolder, final int position) {
        ((RequestBuilder<?>) Glide.with(context).load(videoModels.get(position).getPath()).centerCrop()).into(viewHolder.thumbnail);
        viewHolder.fileName.setText(videoModels.get(position).getName());
        viewHolder.duration.setText(ConvertSecondToHHMMSSString(videoModels.get(position).getDuration()));
        Log.d("TAG", "onBindViewHolder 2 : " + videoModels.get(position).getPath());
        SharedPreferences preferences = context.getSharedPreferences(MY_SHARED_PREFS_VIDEO, MODE_PRIVATE);
        if (preferences != null && !preferences.contains(videoModels.get(position).getPath())) {
            viewHolder.isNewVideoAvailable.setVisibility(View.VISIBLE);
        } else {
            viewHolder.isNewVideoAvailable.setVisibility(View.GONE);
        }
        viewHolder.layout.setOnClickListener(v -> clickListener.onItemClick(viewHolder.getBindingAdapterPosition()));

        viewHolder.filesMenuItem.setOnClickListener(v -> setUpMenuButton(v, viewHolder));

    }

    public int getItemCount() {
        return videoModels.size();
    }

    private void setUpMenuButton(View view, VideoFilesViewHolder holder) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.files_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menuItem_play) {
                holder.layout.performClick();
            } else if (item.getItemId() == R.id.menuItem_share) {
                ShareHelper shareHelper = new ShareHelper(context);
                shareHelper.shareVideo(videoModels.get(holder.getBindingAdapterPosition()).getPath());
            } else if (item.getItemId() == R.id.menuItem_details) {
                Toast.makeText(context, "Details", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popupMenu.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String text) {
        videoModels.clear();
        if (text.isEmpty()) {
            videoModels.addAll(videoModelFull);
        } else {
            text = text.toLowerCase();
            for (VideoModel item : videoModelFull) {
                if (item.getName().toLowerCase().contains(text))
                    videoModels.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClick(final int adapterPotion);
    }

    private String ConvertSecondToHHMMSSString(int nSecondTime) {
        int nSecond = nSecondTime / 1000;
        int hrs = nSecond / 3600;
        int min = (nSecond % 3600) / 60;
        int sec = nSecond % 60;
        if (hrs == 0) {
            return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, min, sec);
        }
    }

    @NonNull
    public VideoFilesViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int n) {
        return new VideoFilesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_rv_files, viewGroup, false));
    }

    public static class VideoFilesViewHolder extends RecyclerView.ViewHolder {
        LayoutRvFilesBinding binding;
        TextView fileName;
        ConstraintLayout layout;
        ImageView thumbnail;
        TextView duration, isNewVideoAvailable;
        ImageButton filesMenuItem;

        public VideoFilesViewHolder(final View view) {
            super(view);
            final LayoutRvFilesBinding bind = LayoutRvFilesBinding.bind(view);
            binding = bind;
            fileName = bind.fileTitle;
            thumbnail = binding.thumbnail;
            layout = binding.filesLayoutRow;
            duration = binding.duration;
            isNewVideoAvailable = binding.textViewNew;
            filesMenuItem = binding.filesMenuItem;
        }
    }
}
