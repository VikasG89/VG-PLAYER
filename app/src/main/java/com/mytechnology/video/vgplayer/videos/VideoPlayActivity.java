package com.mytechnology.video.vgplayer.videos;

import android.annotation.SuppressLint;
import android.app.ComponentCaller;
import android.content.ContentResolver;
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
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
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


public class VideoPlayActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener {
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
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    boolean isScreenLocked = false;

    // Audio Focus Variables
    android.media.AudioAttributes playbackAttributes;
    AudioFocusRequest focusRequest;
    final Object focusLock = new Object();
    boolean playbackDelayed = false;
    boolean playbackNowAuthorized = false;
    boolean resumeOnFocusGain = false;
    public static boolean isVideoPlaying = false;

    // swap & zoom variable
    ConstraintLayout layoutSwapGesture;
    private AudioManager audioManager;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private float initialX, initialY;
    private int displayHeight, displayWidth, displayBrightness, mediaVolume, maxVolume;
    private boolean start = false;
    private boolean leftHorizontal = false;
    private boolean rightHorizontal = false;
    private boolean isControllerVisible = false;
    private static final float PLAY_SPEED_2X = 2f;
    private static final float PLAY_SPEED_NORMAL = 1f;
    ContentResolver contentResolver;
    private Window window;
    boolean value;

    public VideoPlayActivity() {
        this.mvideoModelArrayList = new ArrayList<>();
        this.mediaItemList = new ArrayList<>();
    }

    @OptIn(markerClass = UnstableApi.class)
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        EdgeToEdge.enable(this);
        final ActivityVideoPlayBinding inflate = ActivityVideoPlayBinding.inflate(getLayoutInflater());
        binding = inflate;
        final ConstraintLayout root = inflate.getRoot();
        setContentView(root);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playerView = binding.videoView;
        mainLayout = binding.main;
        previous = playerView.findViewById(R.id.exo_previous);
        next = playerView.findViewById(R.id.exo_next);
        backWard = playerView.findViewById(R.id.exo_backward);
        forward = playerView.findViewById(R.id.exo_forward);
        playPause = playerView.findViewById(R.id.exo_play_pause);
        trackName = playerView.findViewById(R.id.exo_main_text);
        lockScreen = playerView.findViewById(R.id.lockScreen);
        layoutSwapGesture = playerView.findViewById(R.id.layout_swap_gesture);

        preferences = getSharedPreferences("video_player", MODE_PRIVATE);
        editor = preferences.edit();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        playbackAttributes = new android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_GAME)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC).build();
        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes).setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this, Handler.createAsync(Looper.getMainLooper())).build();

        videoFilesAdapterPosition = getIntent().getIntExtra("position", 0);
        mvideoModelArrayList = getIntent().getParcelableArrayListExtra("Parcelable");
        myVFolder = getIntent().getStringExtra("Folder Name");

        player = new ExoPlayer.Builder(this).build();
        playerView.setKeepScreenOn(true);
        playerView.setControllerHideOnTouch(true);

        for (int i = 0; i < mvideoModelArrayList.size(); ++i) {
            mediaItemList.add(MediaItem.fromUri(Uri.parse(mvideoModelArrayList.get(i).getPath())));
        }
        player.setMediaItems(mediaItemList);
        playerView.setPlayer(player);

        // Setting Auto Fullscreen enabled OR disabled
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            if (visibility == View.GONE) {
                setFullScreen(true);
            } else if (visibility == View.VISIBLE) {
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
                    start = false;
                    playerView.hideController();
                    leftHorizontal = false;
                    player.setPlaybackSpeed(PLAY_SPEED_NORMAL);
                }
                return super.onTouch(view, motionEvent);
            }

            @Override
            public void onScrollTouch(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                assert e1 != null;
                start = true;
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                double brightnessSpeed = 0.1;
                try {
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        // Horizontal swipe for playback control
                        if ((Math.abs(deltaX) > SWIPE_THRESHOLD) && (distanceX < 0)) {
                            // Right swipe - Fast forward
                            rightHorizontal = true;
                            Log.d(TAG, "Fast Forward");
                            forWard_10Sec();
                        } else if (((deltaX) < SWIPE_THRESHOLD) && (distanceX > 0)) {
                            // Left swipe - Rewind
                            leftHorizontal = true;
                            playerView.showController();
                            Log.d(TAG, "Rewind");
                            backWard_10Sec();
                        }
                    } else {
                        // Vertical swipe for volume/brightness control
                        value = Settings.System.canWrite(getApplicationContext());
                        if (value) {
                            if (e1.getX() < (float) displayWidth / 2) {
                                // Left half - Brightness
                                contentResolver = getContentResolver();
                                window = getWindow();
                                try {
                                    android.provider.Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                    displayBrightness = android.provider.Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS);
                                } catch (Exception e) {
                                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                                }
                                int calBrightness = (int) (displayBrightness - deltaY * brightnessSpeed);
                                if (calBrightness > 255) {
                                    calBrightness = 255;
                                } else if (calBrightness < 1) {
                                    calBrightness = 1;
                                }
                                double brtPercentage = Math.ceil((((double) calBrightness / (double) 255) * (double) 100));
                                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, calBrightness);
                                WindowManager.LayoutParams layoutParams = window.getAttributes();
                                layoutParams.screenBrightness = displayBrightness / (float) 255;
                                window.setAttributes(layoutParams);
                                Log.d(TAG, "Brightness: " + (int) brtPercentage + "%");
                            } else if (e1.getX() > (float) displayWidth / 2) {
                                // Right half - Volume
                                maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                double calVol = mediaVolume + (-deltaY / displayHeight) * maxVolume;
                                calVol = Math.max(0, Math.min(maxVolume, calVol));
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) calVol, 0);
                                double volPercentage = (calVol / (double) (maxVolume)) * (double) 100;
                                Log.d(TAG, "Max Volume: " + maxVolume);
                                Log.d(TAG, "Media Volume: " + mediaVolume);
                                Log.d(TAG, "Calculated Volume: " + (int) calVol);
                                Log.d(TAG, "Volume: " + (int) volPercentage + "%");
                            }
                        } else {
                            // Request permission to change brightness
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 6547);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(VideoPlayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                super.onScrollTouch(e1, e2, distanceX, distanceY);
            }

            @Override
            public void onDoubleTouch(MotionEvent e) {
                super.onDoubleTouch(e);
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
            }

            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onLongTouch(MotionEvent e) {
                super.onLongTouch(e);
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    player.setPlaybackSpeed(PLAY_SPEED_2X);
                    Log.d(TAG, "Long Touch");
                }
            }

            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onSingleTouch() {
                super.onSingleTouch();
                if (isControllerVisible) {
                    isControllerVisible = false;
                    playerView.hideController();
                } else {
                    isControllerVisible = true;
                    playerView.showController();
                }
                Log.d(TAG, "Single Touch");

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

        /*
        NOT Implemented Yet
        LOCK SCREEN functionality
        */
        /*lockScreen.setOnClickListener(v -> {
            isScreenLocked = !isScreenLocked;
            if (isScreenLocked) {
                lockScreen.setImageResource(R.drawable.lock);
                lockScreen.setVisibility(View.VISIBLE);
                playerView.hideController();
                playerView.setClickable(false);
            } else {
                lockScreen.setImageResource(R.drawable.lock_open);
                playerView.showController();
            }
        });*/

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
        long num = currentPosition - 1000;
        if (num < 0) {
            player.seekTo(0);
        } else {
            player.seekTo(num);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, @NonNull ComponentCaller caller) {
        super.onActivityResult(requestCode, resultCode, data, caller);
        if (requestCode == 6547 && resultCode == RESULT_OK) {
            value = Settings.System.canWrite(getApplicationContext());
            if (value) {
                Log.d(TAG, "Permission Granted");
            } else {
                Log.d(TAG, "Permission Denied");
            }
        } else {
            Log.d(TAG, "Permission Denied");
        }
    }

}
