package com.mytechnology.video.vgplayer.videos;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.LayoutRvFilesBinding;

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

    public void onBindViewHolder(final VideoFilesViewHolder viewHolder, final int position) {
        ((RequestBuilder<?>) Glide.with(context).load(videoModels.get(position).getPath()).centerCrop()).into(viewHolder.thumbnail);
        viewHolder.fileName.setText(videoModels.get(position).getName());
        viewHolder.duration.setText(ConvertSecondToHHMMSSString(videoModels.get(position).getDuration()));
        SharedPreferences preferences = context.getSharedPreferences("video_player", MODE_PRIVATE);
        if (preferences != null && !preferences.contains(videoModels.get(position).getName())) {
            viewHolder.isNew.setVisibility(View.VISIBLE);
        }else {
            viewHolder.isNew.setVisibility(View.GONE);
        }
        viewHolder.layout.setOnClickListener(v -> clickListener.onItemClick(viewHolder.getBindingAdapterPosition()));

        viewHolder.filesMenuItem.setOnClickListener(this::setUpMenuButton);

    }

    public int getItemCount() {
        return videoModels.size();
    }

    private void setUpMenuButton(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.files_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this::handleMenuItemClick);
        popupMenu.show();
    }

    private boolean handleMenuItemClick(MenuItem item) {
        /*if (item.getItemId() == R.id.main_menu_myProfile) {
            startIntent(MainActivity.this, ProfileActivity.class);
        } else if (item.getItemId() == R.id.main_menu_ContactUs) {
            startIntent(MainActivity.this, FAQsActivity.class);
        } else if (item.getItemId() == R.id.main_menu_RateUs) {
            startIntent(MainActivity.this, ReviewActivity.class);
        } else if (item.getItemId() == R.id.main_menu_license_privacy) {
            showLicenses();
        } else if (item.getItemId() == R.id.menu_theme) {
            final Intent intent = new Intent(MainActivity.this, ThemeChangeActivity.class);
            intent.putExtra("BasTu", "Hi!");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }*/
        return true;
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
        CardView layout;
        ImageView thumbnail;
        TextView duration, isNew;
        ImageButton filesMenuItem;

        public VideoFilesViewHolder(final View view) {
            super(view);
            final LayoutRvFilesBinding bind = LayoutRvFilesBinding.bind(view);
            binding = bind;
            fileName = bind.fileTitle;
            thumbnail = binding.thumbnail;
            layout = binding.filesLayoutRow;
            duration = binding.duration;
            isNew = binding.textViewNew;
            filesMenuItem = binding.filesMenuItem;
        }
    }
}
