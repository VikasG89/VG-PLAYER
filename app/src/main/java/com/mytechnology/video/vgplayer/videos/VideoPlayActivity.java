package com.mytechnology.video.vgplayer.videos;

import static com.mytechnology.video.vgplayer.utility.CommonFunctions.ConvertSecondToHHMMSSString;

import android.annotation.SuppressLint;
import android.app.ComponentCaller;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.extractor.ExtractorsFactory;
import androidx.media3.extractor.flac.FlacExtractor;
import androidx.media3.extractor.mp3.Mp3Extractor;
import androidx.media3.ui.PlayerView;

import com.google.android.material.snackbar.Snackbar;
import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.ActivityVideoPlayBinding;
import com.mytechnology.video.vgplayer.utility.OnSwipeListener;
import com.mytechnology.video.vgplayer.utility.ShareHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


@UnstableApi
public class VideoPlayActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener {

    public static final String MY_SHARED_PREFS_VIDEO = "video_player";
    protected ActivityVideoPlayBinding binding;
    private static final String TAG = VideoPlayActivity.class.getSimpleName() + "1";
    private final List<MediaItem> mediaItemList;
    //private final List<MediaSource> mediaSourceList;
    private ArrayList<VideoModel> mVideoModelArrayList;
    PlayerView playerView;
    private ExoPlayer player;
    private ConstraintLayout mainLayout;
    protected ImageView previous, next, backWard, forward, playPause, lockScreen, extraMenu;
    private TextView trackName;
    int videoFilesAdapterPosition;
    static String myVFolder;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    boolean isScreenLocked = false;

    // Audio Focus Variables
    //AudioAttributes playbackAttributes;
    androidx.media3.common.AudioAttributes playbackAttributes;
    //AudioFocusRequest focusRequest;
    final Object focusLock = new Object();
    boolean playbackDelayed = false;
    boolean playbackNowAuthorized = false;
    boolean resumeOnFocusGain = false;
    public static boolean isVideoPlaying = false;

    // swap & zoom variable
    ConstraintLayout layoutSwapGesture;
    private AudioManager audioManager;
    private static final int SWIPE_THRESHOLD = 100;
    private int displayBrightness, mediaVolume, maxVolume;
    private boolean isControllerVisible = false;
    private static final float PLAY_SPEED_2X = 2f;
    private static final float PLAY_SPEED_NORMAL = 1f;
    ContentResolver contentResolver;
    private Window window;
    boolean value;
    ConstraintLayout controllerMainLayout;
    LinearLayout swapBackward, swapForward;
    TextView txtFastForward10, txtBackward10;

    // volume and brightness Variable
    private ConstraintLayout volumeLayout, brightnessLayout, fastForwardBackward;
    private ImageView imageViewVolume, imageViewBrightness;
    private ProgressBar progressBarVolume, progressBarBrightness, timeBar;
    private TextView txtVolumeText, txBrightnessText, txtFastForwardBackward;
    private ImageView playPauseDoubleTap;
    int doubleTapCountLeft = 0;
    int doubleTapCountRight = 0;

    public VideoPlayActivity() {
        //this.mediaSourceList = new ArrayList<>();
        this.mVideoModelArrayList = new ArrayList<>();
        this.mediaItemList = new ArrayList<>();
    }

    @OptIn(markerClass = UnstableApi.class)
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        EdgeToEdge.enable(this);
        binding = ActivityVideoPlayBinding.inflate(getLayoutInflater());
        final ConstraintLayout root = binding.getRoot();
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
        lockScreen = binding.lockScreen;
        layoutSwapGesture = mainLayout.findViewById(R.id.layout_swap_gesture);
        controllerMainLayout = playerView.findViewById(R.id.layout_player_controller);
        volumeLayout = layoutSwapGesture.findViewById(R.id.layout_swap_gesture_volume);
        brightnessLayout = layoutSwapGesture.findViewById(R.id.layout_swap_gesture_brightness);
        fastForwardBackward = layoutSwapGesture.findViewById(R.id.layout_swap_gesture_fast_forward_backward);
        txtFastForwardBackward = layoutSwapGesture.findViewById(R.id.txtfast_forward_backward_position);
        timeBar = layoutSwapGesture.findViewById(R.id.timeBar);
        swapForward = layoutSwapGesture.findViewById(R.id.layout_swap_gesture_fast_forward_10);
        swapBackward = layoutSwapGesture.findViewById(R.id.layout_swap_gesture_backward_10);
        txtFastForward10 = layoutSwapGesture.findViewById(R.id.txtIncrement10);
        txtBackward10 = layoutSwapGesture.findViewById(R.id.txtDecrement10);
        imageViewVolume = volumeLayout.findViewById(R.id.imageViewVolume);
        imageViewBrightness = brightnessLayout.findViewById(R.id.imageViewBrightness);
        progressBarVolume = volumeLayout.findViewById(R.id.progressBarVolume);
        progressBarBrightness = brightnessLayout.findViewById(R.id.progressBarBrightness);
        txtVolumeText = volumeLayout.findViewById(R.id.txtVolumeText);
        txBrightnessText = brightnessLayout.findViewById(R.id.txBrightnessText);
        playPauseDoubleTap = layoutSwapGesture.findViewById(R.id.imageViewPlayPauseDoubleTap);
        extraMenu = playerView.findViewById(R.id.setting_list_Menu);

        preferences = getSharedPreferences(MY_SHARED_PREFS_VIDEO, MODE_PRIVATE);
        editor = preferences.edit();


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        /*playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes).setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, Handler.createAsync(Looper.getMainLooper())).build();
        }*/

        playbackAttributes = new AudioAttributes.Builder().build();

        /*focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build();*/

        // Getting Intent Data
        videoFilesAdapterPosition = getIntent().getIntExtra("position", 0);
        mVideoModelArrayList = getIntent().getParcelableArrayListExtra("Parcelable");
        myVFolder = getIntent().getStringExtra("Folder Name");

        // Initialize ExoPlayer
        initializeExoPlayer();


        // Setting Auto Fullscreen enabled OR disabled
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            if (visibility == View.GONE) {
                setFullScreen(true);
                lockScreen.setVisibility(View.INVISIBLE);
            } else if (visibility == View.VISIBLE) {
                setFullScreen(false);
                lockScreen.setVisibility(View.VISIBLE);
            }
        });

        // SwapGesture implementation
        playerView.setOnTouchListener(new OnSwipeListener(this) {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (controllerMainLayout.getVisibility() == View.VISIBLE) {
                            playerView.hideController();
                            lockScreen.setVisibility(View.VISIBLE);
                        } else if (!playerView.isControllerFullyVisible() && isScreenLocked) {
                            lockScreen.setVisibility(View.VISIBLE);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        player.setPlaybackSpeed(PLAY_SPEED_NORMAL);
                        volumeLayout.setVisibility(View.GONE);
                        brightnessLayout.setVisibility(View.GONE);
                        fastForwardBackward.setVisibility(View.GONE);
                        swapForward.setVisibility(View.GONE);
                        swapBackward.setVisibility(View.GONE);
                        if (player != null && player.isPlaying()) {
                            playPauseDoubleTap.setVisibility(View.GONE);
                        }
                        break;
                }
                return super.onTouch(view, motionEvent);
            }

            @Override
            public void onScrollTouch(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                assert e1 != null;
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                double brightnessSpeed = 0.01;
                if (!isScreenLocked) {
                    try {
                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            timeBar.setMin(0);
                            timeBar.setMax((int) player.getDuration());
                            // Horizontal swipe for playback control
                            if ((Math.abs(deltaX) > SWIPE_THRESHOLD) && (distanceX < 0)) {
                                // Right swipe - Fast forward
                                forWard_10Sec();
                                fastForwardBackward.setVisibility(View.VISIBLE);
                                txtFastForwardBackward.setText(ConvertSecondToHHMMSSString((int) player.getCurrentPosition()));
                                timeBar.setProgress((int) player.getCurrentPosition());
                            } else if (((deltaX) < SWIPE_THRESHOLD) && (distanceX > 0)) {
                                // Left swipe - Rewind
                                backWard_10Sec();
                                fastForwardBackward.setVisibility(View.VISIBLE);
                                txtFastForwardBackward.setText(ConvertSecondToHHMMSSString((int) player.getCurrentPosition()));
                                timeBar.setProgress((int) player.getCurrentPosition());
                            }
                        } else {
                            // Vertical swipe for volume/brightness control
                            value = Settings.System.canWrite(getApplicationContext());
                            if (value) {
                                if (e1.getX() < (float) mainLayout.getWidth() / 2) {
                                    // Left half - Brightness
                                    contentResolver = getContentResolver();
                                    window = getWindow();
                                    try {
                                        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                                        displayBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
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
                                    if (brtPercentage < 30) {
                                        imageViewBrightness.setImageResource(R.drawable.brightness_low);
                                    } else if (brtPercentage > 31 && brtPercentage < 70) {
                                        imageViewBrightness.setImageResource(R.drawable.brightness_medium);
                                    } else if (brtPercentage > 71) {
                                        imageViewBrightness.setImageResource(R.drawable.brightness_high);
                                    }
                                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, calBrightness);
                                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                                    layoutParams.screenBrightness = displayBrightness / (float) 255;
                                    window.setAttributes(layoutParams);

                                    brightnessLayout.setVisibility(View.VISIBLE);
                                    progressBarBrightness.setProgress((int) brtPercentage);
                                    txBrightnessText.setText(String.format(Locale.getDefault(), "%d%%", (int) brtPercentage));
                                    hideControllerSwipe();
                                } else if (e1.getX() > (float) mainLayout.getWidth() / 2) {
                                    // Right half - Volume
                                    maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                    mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                    double calVol = mediaVolume + (-deltaY / ((double) mainLayout.getHeight() /*/ 2*/)) * maxVolume;
                                    calVol = Math.max(0, Math.min(maxVolume, calVol));
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) calVol, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                    double volPercentage = (calVol / (double) (maxVolume)) * (double) 100;
                                    volumeLayout.setVisibility(View.VISIBLE);
                                    if (volPercentage < 1) {
                                        imageViewVolume.setImageResource(R.drawable.volume_off);
                                    } else {
                                        imageViewVolume.setImageResource(R.drawable.volume_up);
                                    }
                                    progressBarVolume.setProgress((int) volPercentage);
                                    txtVolumeText.setText(String.format(Locale.getDefault(), "%d%%", (int) volPercentage));
                                    hideControllerSwipe();
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
                }
                super.onScrollTouch(e1, e2, distanceX, distanceY);
            }

            @Override
            public void onDoubleTouch(MotionEvent e) {
                super.onDoubleTouch(e);
                boolean left = e.getX() < (float) mainLayout.getWidth() / 3;
                boolean right = e.getX() > ((float) mainLayout.getWidth() / 3) * 2;
                boolean center = e.getX() > (float) mainLayout.getWidth() / 3 && e.getX() < ((float) mainLayout.getWidth() / 3) * 2;
                if (!isScreenLocked) {
                    if (left) {
                        // Left half - For rewind on Double Tap
                        doubleTapCountRight = 0;
                        doubleTapCountLeft ++;
                        Log.d(TAG, "onDoubleTouch: " + doubleTapCountLeft);
                        backWard_10Sec();
                        swapBackward.setVisibility(View.VISIBLE);
                        lockScreen.setVisibility(View.GONE);
                        int increment = 0;
                        if (player.getCurrentPosition() != timeBar.getMin()){
                            increment = doubleTapCountLeft * 10;
                        }
                        txtBackward10.setText(String.format(Locale.getDefault(),"-%d", increment));
                    } else if (right) {
                        // Right half - For Fast Forward on Double Tap
                        doubleTapCountLeft = 0;
                        doubleTapCountRight++;
                        forWard_10Sec();
                        swapForward.setVisibility(View.VISIBLE);
                        lockScreen.setVisibility(View.GONE);
                        int increment = 0;
                        if (player.getCurrentPosition() != timeBar.getMax()){
                            increment = doubleTapCountRight * 10;
                        }
                        txtFastForward10.setText(String.format(Locale.getDefault(),"+%d", increment));
                    } else if (center) {
                        if (player.isPlaying()) {
                            pauseVideo();
                            playPauseDoubleTap.setVisibility(View.VISIBLE);
                            playPauseDoubleTap.setImageResource(R.drawable.play);
                        } else {
                            playVideo();
                            playPauseDoubleTap.setVisibility(View.VISIBLE);
                            playPauseDoubleTap.setImageResource(R.drawable.pause);
                        }
                    }
                }
            }

            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onLongTouch(MotionEvent e) {
                super.onLongTouch(e);
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isScreenLocked) {
                        player.setPlaybackSpeed(PLAY_SPEED_2X);
                    }
                }
            }

            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onSingleTouch() {
                super.onSingleTouch();
                doubleTapCountLeft = 0;
                doubleTapCountRight = 0;
                isControllerVisible = !isControllerVisible;
                if (!isScreenLocked) {
                    if (isControllerVisible) {
                        playerView.hideController();
                        controllerMainLayout.setVisibility(View.GONE);
                        lockScreen.setVisibility(View.INVISIBLE);
                    } else {
                        playerView.showController();
                        lockScreen.setVisibility(View.VISIBLE);
                        controllerMainLayout.setVisibility(View.VISIBLE);
                    }
                }
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
            isScreenLocked = !isScreenLocked;
            if (isScreenLocked) {
                lockScreen.setImageResource(R.drawable.lock);
                lockScreen.setVisibility(View.VISIBLE);
                playerView.hideController();
                controllerMainLayout.setVisibility(View.GONE);
            } else {
                lockScreen.setImageResource(R.drawable.lock_open);
                lockScreen.setVisibility(View.VISIBLE);
                playerView.showController();
                controllerMainLayout.setVisibility(View.VISIBLE);
            }
        });

        extraMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(VideoPlayActivity.this, extraMenu);
            popupMenu.getMenuInflater().inflate(R.menu.video_play_menu_list, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.video_play_list_share) {
                    if (player.isPlaying()) {
                        pauseVideo();
                    }
                    ShareHelper shareHelper = new ShareHelper(VideoPlayActivity.this);
                    shareHelper.shareVideo(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath());
                } else if (item.getItemId() == R.id.video_play_list_properties) {
                    if (player.isPlaying()) {
                        pauseVideo();
                    }
                    AlertDialog dialog = new AlertDialog.Builder(VideoPlayActivity.this).create();
                    dialog.setTitle("Properties - ");
                    dialog.setIcon(R.drawable.propertise_info);
                    String height;
                    String width;
                    try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                        retriever.setDataSource(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath());
                        height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                        width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    File file = new File(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath());
                    // Get last modified date
                    long lastModified = file.lastModified();
                    //Format the date and time
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    String formattedDate = sdf.format(new Date(lastModified));
                    dialog.setMessage("Name:  " + mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getName() + "\n\n"
                            + "Video Location:  " + mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath() + "\n\n"
                            + "Resolution:  " + width + " X " + height + "\n\n"
                            + "Size:  " + Formatter.formatFileSize(VideoPlayActivity.this, mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getSize()) + "\n\n"
                            + "Duration:  " + ConvertSecondToHHMMSSString(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getDuration()) + "\n\n"
                            + "Date Added Or Modified: \n" + "\t\t\t\t\t\t" + formattedDate + "\n\n");
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog1, which) -> {
                        playVideo();
                        dialog1.dismiss();
                    });
                    dialog.show();
                }
                return true;
            });
            popupMenu.show();
        });

        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    trackName.setText(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getName());
                    editor.putLong(mVideoModelArrayList.get(player.getCurrentMediaItemIndex() - 1).getPath(), -1);
                    editor.commit();
                    // If video previously played then show snack bar
                    if (preferences.contains(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath())) {
                        Snackbar.make(mainLayout, "Play from Start!", 4000)
                                .setAction("Yes", v -> {
                                    player.seekTo(0);
                                    playVideo();
                                })
                                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                                .show();
                    }
                } else if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) {
                    savePreference(player.getCurrentMediaItemIndex());
                    finish();
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
                //mediaSourceList.clear();
                mediaItemList.clear();
                Intent intent = new Intent(VideoPlayActivity.this, VideoFilesActivity.class);
                intent.putExtra("Folder Name", myVFolder);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    private void resetDoubleTap(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                doubleTapCountLeft = 0;
                doubleTapCountRight = 0;
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 2000);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void hideControllerSwipe() {
        controllerMainLayout.setVisibility(View.GONE);
        lockScreen.setVisibility(View.GONE);
        playerView.hideController();
        setFullScreen(true);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializeExoPlayer() {
        RenderersFactory renderersFactory = new DefaultRenderersFactory(this).setEnableDecoderFallback(true)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
                .setMp3ExtractorFlags(Mp3Extractor.FLAG_ENABLE_INDEX_SEEKING)
                .setFlacExtractorFlags(FlacExtractor.FLAG_DISABLE_ID3_METADATA);
        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(this)
                .setDataSourceFactory(new DefaultDataSource.Factory(this));
        player = new ExoPlayer.Builder(this, renderersFactory)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this, extractorsFactory))
                .setMediaSourceFactory(mediaSourceFactory)
                .setHandleAudioBecomingNoisy(true)
                .setAudioAttributes(playbackAttributes, true)
                .build();

        for (int i = 0; i < mVideoModelArrayList.size(); ++i) {
           /* MediaSource mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(Uri.parse(mVideoModelArrayList.get(i).getPath())));
            mediaSourceList.add(mediaSource);*/
            mediaItemList.add(MediaItem.fromUri(Uri.parse(mVideoModelArrayList.get(i).getPath())));
        }
        player.setMediaItems(mediaItemList);
        //player.setMediaSources(mediaSourceList);
        playerView.setPlayer(player);
        // Call Play Video Method
        playVideo1(videoFilesAdapterPosition);
    }

    private void playVideo1(int currentPosition) {
        player.prepare();
        if (preferences != null && preferences.contains(mVideoModelArrayList.get(currentPosition).getPath())) {
            long position1 = preferences.getLong(mVideoModelArrayList.get(currentPosition).getPath(), 0);
            if (position1 != player.getDuration()) {
                player.seekTo(currentPosition, position1);
                Snackbar.make(mainLayout, "Play from Start!", 3000)
                        .setAction("Yes", v -> {
                            player.seekTo(0);
                            playVideo();
                        })
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show();
            } else {
                player.seekTo(currentPosition, 0);
            }
        } else {
            player.seekTo(currentPosition, 0);
        }
        player.setPlayWhenReady(true);
        trackName.setText(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getName());

        /*int res = audioManager.requestAudioFocus(focusRequest);
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
        }*/
        savePreference(player.getCurrentPosition());
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
        if (position > mVideoModelArrayList.size() - 1) {
            position = mVideoModelArrayList.size() - 1;
            playVideo1(position);
        } else {
            playVideo1(position);
        }
    }

    private void savePreference(long currentPlayedTime) {
        editor.putLong(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath(), currentPlayedTime);
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

   /* @SuppressLint("SourceLockedOrientationActivity")
    private void changeOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }*/

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
