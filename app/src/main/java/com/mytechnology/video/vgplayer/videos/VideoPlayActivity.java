package com.mytechnology.video.vgplayer.videos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.ActivityVideoPlayBinding;
import com.mytechnology.video.vgplayer.utility.OnSwipeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class VideoPlayActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener/*, GestureDetector.OnGestureListener*/ {
    protected ActivityVideoPlayBinding binding;
    private static final String TAG = VideoPlayActivity.class.getSimpleName() + "1";

    private final List<MediaItem> mediaItemList;
    private ArrayList<VideoModel> mvideoModelArrayList;
    PlayerView playerView;
    private ExoPlayer player;
    private ConstraintLayout mainLayout;
    protected ImageView previous, next, backWard, forward, playPause, lockScreen;
    private TextView trackName;
    int videoFilesAdapterPosition;
    String myVFolder;
    //private GestureDetector gestureDetector;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private AudioManager audioManager;
    private int maxVolume;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private float initialX, initialY;

    android.media.AudioAttributes playbackAttributes;
    AudioFocusRequest focusRequest;
    final Object focusLock = new Object();

    boolean playbackDelayed = false;
    boolean playbackNowAuthorized = false;
    boolean resumeOnFocusGain = false;
    public static boolean isVideoPlaying = false;

    // swap & zoom variable
    private int displayHeight, displayWidth;
    private boolean start = false;
    private boolean left, right;
    private static final float PLAY_SPEED_2X = 2f;
    private static final float PLAY_SPEED_NORMAL = 1f;

    public VideoPlayActivity() {
        this.mvideoModelArrayList = new ArrayList<>();
        this.mediaItemList = new ArrayList<>();
    }

    @OptIn(markerClass = UnstableApi.class)
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        final ActivityVideoPlayBinding inflate = ActivityVideoPlayBinding.inflate(getLayoutInflater());
        binding = inflate;
        final ConstraintLayout root = inflate.getRoot();
        setContentView(root);
        playerView = binding.videoView;
        mainLayout = binding.main;
        previous = playerView.findViewById(R.id.exo_previous);
        next = playerView.findViewById(R.id.exo_next);
        backWard = playerView.findViewById(R.id.exo_backward);
        forward = playerView.findViewById(R.id.exo_forward);
        playPause = playerView.findViewById(R.id.exo_play_pause);
        trackName = playerView.findViewById(R.id.exo_main_text);
        lockScreen = playerView.findViewById(R.id.lockScreen);

        preferences = getSharedPreferences("video_player", MODE_PRIVATE);
        editor = preferences.edit();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        playbackAttributes = new android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_GAME)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this, Handler.createAsync(Looper.getMainLooper()))
                .build();

        videoFilesAdapterPosition = getIntent().getIntExtra("position", 0);
        mvideoModelArrayList = getIntent().getParcelableArrayListExtra("Parcelable");
        myVFolder = getIntent().getStringExtra("Folder Name");

        player = new ExoPlayer.Builder(this).build();
        playerView.setKeepScreenOn(true);
        playerView.setControllerShowTimeoutMs(1000);

        for (int i = 0; i < mvideoModelArrayList.size(); ++i) {
            mediaItemList.add(MediaItem.fromUri(Uri.parse(mvideoModelArrayList.get(i).getPath())));
        }
        player.setMediaItems(mediaItemList);
        playerView.setPlayer(player);

        // Setting Auto Fullscreen enabled OR disabled
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            if (visibility == 8) {
                setFullScreen(true);
            } else if (visibility == 0) {
                setFullScreen(false);
            }
        });

        // Call Play Video Method
        playVideo1(videoFilesAdapterPosition);

        // SwapGesture implementation
        playerView.setOnTouchListener(new OnSwipeListener(this) {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    player.setPlaybackSpeed(PLAY_SPEED_NORMAL);
                }
                return super.onTouch(view, motionEvent);
            }

            @Override
            public void onScrollTouch(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                assert e1 != null;
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                try {
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        // Horizontal swipe for playback control
                        if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                            // Right swipe - Fast forward
                            Log.d(TAG, "Fast Forward");
                            forWard_10Sec();
                        } else if ((deltaX) < (SWIPE_THRESHOLD)) {
                            // Left swipe - Rewind
                            Log.d(TAG, "Rewind");
                            backWard_10Sec();
                        }
                    } else {
                        // Vertical swipe for volume/brightness control
                        if (e1.getX() < (float) mainLayout.getWidth() / 2) {
                            // Left half - Brightness
                            Log.d(TAG, "Brightness");
                            adjustBrightness(-deltaY / mainLayout.getHeight());
                        } else if (e1.getX() > (float) mainLayout.getWidth() / 2) {
                            // Right half - Volume
                            Log.d(TAG, "Volume");
                            adjustVolume(-deltaY / mainLayout.getHeight());
                        }
                    }
                } catch (
                        Exception e) {
                    Toast.makeText(VideoPlayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                super.onScrollTouch(e1, e2, distanceX, distanceY);
            }

            @Override
            public void onDoubleTouch(MotionEvent e) {
                assert e != null;
                if (e.getX() < (float) mainLayout.getWidth() / 2) {
                    // Left half - For rewind on Double Tap
                    Log.d(TAG, "Rewind on Double Tap");
                    backWard_10Sec();
                } else if (e.getX() > (float) mainLayout.getWidth() / 2) {
                    // Right half - For Fast Forward on Double Tap
                    Log.d(TAG, "Fast Forward on Double Tap ");
                    forWard_10Sec();
                }
                super.onDoubleTouch(e);
            }

            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onLongTouch(MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    player.setPlaybackSpeed(PLAY_SPEED_2X);
                    Log.d(TAG, "Long Touch");
                }
                super.onLongTouch(e);
            }

            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onSingleTouch() {
                playerView.showController();
                player.setPlaybackSpeed(PLAY_SPEED_NORMAL);
                Log.d(TAG, "Single Touch");
                super.onSingleTouch();
            }
        });

        previous.setOnClickListener(v -> previousVideoPlay());

        next.setOnClickListener(v -> nextVideoPlay());

        backWard.setOnClickListener(v -> backWard_10Sec());

        forward.setOnClickListener(v -> forWard_10Sec());

        playPause.setOnClickListener(v -> {
            if (player.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        });

        lockScreen.setOnClickListener(v -> {
            lockScreen.setImageResource(R.drawable.lock);

        });

        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    trackName.setText(mvideoModelArrayList.get(player.getCurrentMediaItemIndex()).getName());
                    editor.putLong(mvideoModelArrayList.get(player.getCurrentMediaItemIndex() - 1).getName(), -1);
                    editor.commit();
                    //Toast.makeText(VideoPlayActivity.this, "Play from Start!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                Log.d(TAG, Objects.requireNonNull(error.getMessage()));
                Toast.makeText(VideoPlayActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }

        });

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (player.isPlaying()) {
                    pauseVideo();
                    savePreference(player.getCurrentPosition());
                    player.stop();

                }
                player.release();
                mediaItemList.clear();
                Intent intent = new Intent(VideoPlayActivity.this, VideoFilesActivity.class);
                intent.putExtra("Folder Name", myVFolder);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    private void playVideo1(int currentPosition) {
        player.prepare();
        if (preferences != null && preferences.contains(mvideoModelArrayList.get(currentPosition).getName())) {
            long position1 = preferences.getLong(mvideoModelArrayList.get(currentPosition).getName(), 0);
            if (position1 != player.getDuration()) {
                player.seekTo(currentPosition, position1);
            } else {
                player.seekTo(currentPosition, 0);
            }
        } else {
            player.seekTo(currentPosition, 0);
        }
        player.setPlayWhenReady(true);
        trackName.setText(mvideoModelArrayList.get(player.getCurrentMediaItemIndex()).getName());

        int res = audioManager.requestAudioFocus(focusRequest);
        synchronized (focusLock) {
            if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                playbackNowAuthorized = false;
            } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                playbackNowAuthorized = true;
                playVideo();
            } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                playbackDelayed = true;
                playbackNowAuthorized = false;
            }
        }
    }

    private void playVideo() {
        player.setPlayWhenReady(true);
        isVideoPlaying = true;
        playPause.setImageResource(R.drawable.pause);
    }

    private void handleCallOrCommunicationState() {
        if (playbackDelayed || resumeOnFocusGain) {
            synchronized (focusLock) {
                playbackDelayed = false;
                resumeOnFocusGain = false;
                pauseVideo();
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized (focusLock) {
                        playbackDelayed = false;
                        resumeOnFocusGain = false;
                    }
                    playVideo();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                synchronized (focusLock) {
                    resumeOnFocusGain = (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) && player.isPlaying();
                    playbackDelayed = false;
                }
                pauseVideo();
                break;
            case AudioManager.MODE_IN_CALL:
            case AudioManager.MODE_IN_COMMUNICATION:
                handleCallOrCommunicationState();
                break;
            default:
                Log.w("AudioFocus", "Unexpected focus change: " + focusChange);
                pauseVideo();
        }
    }

    private void previousVideoPlay() {
        savePreference(player.getCurrentPosition());
        player.stop();
        int position = player.getCurrentMediaItemIndex();
        position--;
        if (position < 0) {
            position = 0;
            playVideo1(position);
        } else {
            playVideo1(position);
        }
    }

    private void nextVideoPlay() {
        savePreference(player.getCurrentPosition());
        player.stop();
        int position = player.getCurrentMediaItemIndex();
        position++;
        if (position > mvideoModelArrayList.size() - 1) {
            position = mvideoModelArrayList.size() - 1;
            playVideo1(position);
        } else {
            playVideo1(position);
        }
    }

    private void clearSurface() {
        SurfaceView surfaceView = new SurfaceView(this);
        player.clearVideoSurfaceHolder(surfaceView.getHolder());
        SurfaceHolder holder = surfaceView.getHolder();
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.BLACK);
        holder.unlockCanvasAndPost(canvas);
        player.setVideoSurfaceHolder(holder);
        Log.d(TAG, "Surface Cleared");
    }

    private void savePreference(long currentPlayedTime) {
        editor.putLong(mvideoModelArrayList.get(player.getCurrentMediaItemIndex()).getName(), currentPlayedTime);
        editor.commit();
    }

    private void pauseVideo() {
        player.pause();
        isVideoPlaying = false;
        playPause.setImageResource(R.drawable.play);
    }

    private void forWard_10Sec() {
        player.seekTo(player.getCurrentPosition() + 10000);
    }

    private void backWard_10Sec() {
        long currentPosition = player.getCurrentPosition();
        long num = currentPosition - 10000;
        if (num < 0) {
            player.seekTo(0);
        } else {
            player.seekTo(num - 10000);
        }
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

    private void adjustVolume(float change) {
        int volume = (int) (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + change * maxVolume);
        volume = Math.max(0, Math.min(maxVolume, volume));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    private void adjustBrightness(float change) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = Math.max(0.01f, Math.min(1f, layoutParams.screenBrightness + change));
        getWindow().setAttributes(layoutParams);
    }

    @Override
    protected void onPause() {
        if (player.isPlaying()) {
            pauseVideo();
            savePreference(player.getCurrentPosition());
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!player.isPlaying()) {
            playVideo();
        }
    }

    @Override
    protected void onDestroy() {
        if (player.isPlaying()) {
            savePreference(player.getCurrentPosition());
            player.stop();
            player.release();
        }
        super.onDestroy();
    }

}
