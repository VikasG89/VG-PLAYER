package com.mytechnology.video.vgplayer.videos;

import static android.content.Context.MODE_PRIVATE;
import static androidx.appcompat.app.AlertDialog.Builder;
import static com.mytechnology.video.vgplayer.utility.CommonFunctions.ConvertSecondToHHMMSSString;
import static com.mytechnology.video.vgplayer.videos.VideoPlayActivity.MY_SHARED_PREFS_VIDEO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.LayoutRvFilesBinding;
import com.mytechnology.video.vgplayer.utility.ShareHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.VideoFilesViewHolder> {
    Context context;
    ArrayList<VideoModel> videoModels;
    private final ItemClickListener clickListener;
    ArrayList<VideoModel> videoModelFull;
    ArrayList<VideoModel> selectedVideoModels;
    boolean multiSelection = false; // Flag to track if multiple items are selected

    public VideoFilesAdapter(final Context context, final ArrayList<VideoModel> videoModels, ItemClickListener clickListener) {
        this.context = context;
        this.videoModels = videoModels;
        this.clickListener = clickListener;
        this.videoModelFull = new ArrayList<>(videoModels);
        this.selectedVideoModels = new ArrayList<>();
    }


    @NonNull
    public VideoFilesViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int n) {
        return new VideoFilesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_rv_files, viewGroup, false));
    }

    @SuppressLint("NotifyDataSetChanged")
    @OptIn(markerClass = UnstableApi.class)
    public void onBindViewHolder(final VideoFilesViewHolder viewHolder, final int position) {
        ((RequestBuilder<?>) Glide.with(context).load(videoModels.get(position).getPath()).centerCrop()).into(viewHolder.thumbnail);
        viewHolder.fileName.setText(videoModels.get(position).getName());
        viewHolder.duration.setText(ConvertSecondToHHMMSSString(videoModels.get(position).getDuration()));
        SharedPreferences preferences = context.getSharedPreferences(MY_SHARED_PREFS_VIDEO, MODE_PRIVATE);
        if (preferences != null && !preferences.contains(videoModels.get(position).getPath())) {
            viewHolder.isNewVideoAvailable.setVisibility(View.VISIBLE);
        }

        if (multiSelection) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.filesMenu.setVisibility(View.GONE);
            viewHolder.checkBox.setChecked(selectedVideoModels.contains(videoModels.get(position)));
        } else {
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.filesMenu.setVisibility(View.VISIBLE);
        }

        viewHolder.layout.setOnClickListener(v -> {
            if (multiSelection) {
                if (selectedVideoModels.contains(videoModels.get(position))) {
                    selectedVideoModels.remove(videoModels.get(position));
                    viewHolder.checkBox.setChecked(false);
                } else {
                    selectedVideoModels.add(videoModels.get(position));
                    viewHolder.checkBox.setChecked(true);
                }
                VideoFilesActivity.actionMode.setTitle(selectedVideoModels.size() + " Video Selected");
            } else {
                clickListener.onItemClick(position, viewHolder);
            }
            notifyItemChanged(position);
            notifyDataSetChanged();
        });

        viewHolder.filesMenu.setOnClickListener((View v) -> setUpMenuButton(v, viewHolder, position));

        viewHolder.layout.setOnLongClickListener(v -> {
            clickListener.longClick(position, viewHolder);
            return true;
        });

    }

    public int getItemCount() {
        return videoModels.size();
    }

    private void setUpMenuButton(View view, VideoFilesViewHolder holder, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.files_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menuItem_play) {
                holder.layout.performClick();
            } else if (item.getItemId() == R.id.menuItem_share) {
                ShareHelper shareHelper = new ShareHelper(context);
                shareHelper.shareVideo(videoModels.get(position).getPath());
            } else if (item.getItemId() == R.id.menuItem_details) {
                AlertDialog dialog = new Builder(context).create();
                dialog.setIcon(R.drawable.propertise_info);
                dialog.setTitle("Properties: ");
                String height;
                String width;
                try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                    retriever.setDataSource(videoModels.get(position).getPath());
                    height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                File file = new File(videoModels.get(position).getPath());
                // Get last modified date
                long lastModified = file.lastModified();
                //Format the date and time
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                String formattedDate = sdf.format(new Date(lastModified));
                dialog.setMessage("Name:  " + videoModels.get(position).getName() + "\n\n"
                        + "Video Location:  " + videoModels.get(position).getPath() + "\n\n"
                        + "Resolution:  " + width + " X " + height + "\n\n"
                        + "Size:  " + Formatter.formatFileSize(context, videoModels.get(position).getSize()) + "\n\n"
                        + "Duration:  " + ConvertSecondToHHMMSSString(videoModels.get(position).getDuration()) + "\n\n"
                        + "Date Added Or Modified: \n" + "\t\t\t\t\t\t" + formattedDate + "\n\n");
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog1, which) -> dialog1.dismiss());
                dialog.show();
            } else if (item.getItemId() == R.id.menuItem_delete) {
                clickListener.deleteFile(position);
            } else if (item.getItemId() == R.id.menuItem_rename) {
                clickListener.reNameFile(position);
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
        void onItemClick(final int position, VideoFilesViewHolder viewHolder);

        void longClick(final int position, VideoFilesViewHolder viewHolder);

        void deleteFile(int position);

        void reNameFile(int position);

    }

    public static class VideoFilesViewHolder extends RecyclerView.ViewHolder {
        LayoutRvFilesBinding binding;
        TextView fileName;
        ConstraintLayout layout;
        ImageView thumbnail;
        TextView duration, isNewVideoAvailable;
        ImageView filesMenu;
        CheckBox checkBox;

        public VideoFilesViewHolder(final View view) {
            super(view);
            final LayoutRvFilesBinding bind = LayoutRvFilesBinding.bind(view);
            binding = bind;
            fileName = bind.fileTitle;
            thumbnail = binding.thumbnail;
            layout = binding.filesLayoutRow;
            duration = binding.duration;
            isNewVideoAvailable = binding.textViewNew;
            filesMenu = binding.filesMenuItem;
            checkBox = binding.checkBox;
        }
    }
}
