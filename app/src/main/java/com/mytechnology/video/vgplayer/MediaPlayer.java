package com.mytechnology.video.vgplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.util.Objects;

public class MediaPlayer extends AppCompatActivity {
    private PlayerView playerView;
    private TextView txtSongName;
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_media_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        playerView = findViewById(R.id.media_player_view);
        txtSongName = findViewById(R.id.txt_song_Name_media);
        player = new ExoPlayer.Builder(this).build();

        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();
        if ("android.intent.action.VIEW".equals(action) ||
                Objects.equals(type, "audio/*") || Objects.equals(type, "video/*")) {
            if (type != null && type.equals("video/*")){
                playerView.setKeepScreenOn(true);
                playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
                    if (visibility == 8) {
                        setFullScreen(true);
                    } else if (visibility == 0) {
                        setFullScreen(false);
                    }
                });
            }
            playBroadcastAudio(Uri.parse(intent.getDataString()));
        }

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (player != null) {
                    player.stop();
                    player.release();
                    finish();
                }
            }
        });
    }

    private void setFullScreen(boolean isFullScreen) {
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(this.getWindow(), this.getWindow().getDecorView());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        if (isFullScreen) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars());
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    @Override
    protected void onPause() {
        if (player != null) {
            player.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void playBroadcastAudio(final Uri uri) {
        playerView.setShowSubtitleButton(true);
        playerView.setKeepScreenOn(false);
        MediaItem mediaItem = new MediaItem.Builder().setMediaId("")
                .setUri(uri).build();
        MediaMetadata metadata = new MediaMetadata.Builder().setArtist(mediaItem.mediaMetadata.artist)
                .setTitle(mediaItem.mediaMetadata.title).build();
        mediaItem = new MediaItem.Builder().setMediaMetadata(metadata).setUri(uri).build();
        player.addMediaItem(mediaItem);
        playerView.setPlayer(player);
        player.prepare();
        txtSongName.setText(mediaItem.mediaMetadata.title);
        player.setPlayWhenReady(true);
    }
}