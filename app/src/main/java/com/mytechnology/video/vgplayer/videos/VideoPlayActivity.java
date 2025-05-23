package com.mytechnology.video.vgplayer.videos;

import static com.mytechnology.video.vgplayer.utility.CommonFunctions.ConvertSecondToHHMMSSString;

import android.annotation.SuppressLint;
import android.app.ComponentCaller;
import android.app.UiModeManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.DefaultEncoderFactory;
import androidx.media3.transformer.DefaultMuxer;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.Transformer;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.PlayerView;

import com.google.android.material.snackbar.Snackbar;
import com.mytechnology.video.vgplayer.MainActivity;
import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.databinding.ActivityVideoPlayBinding;
import com.mytechnology.video.vgplayer.utility.OnSwipeListener;
import com.mytechnology.video.vgplayer.utility.ShareHelper;
import com.mytechnology.video.vgplayer.utility.SpeedControl;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@UnstableApi
public class VideoPlayActivity extends AppCompatActivity {
    public static final String MY_SHARED_PREFS_VIDEO = "video_player";
    protected ActivityVideoPlayBinding binding;
    private static final String TAG = VideoPlayActivity.class.getSimpleName() + "1";
    private final List<MediaItem> mediaItemList;
    private ArrayList<VideoModel> mVideoModelArrayList;
    PlayerView playerView;
    private ExoPlayer player;
    private ConstraintLayout mainLayout;
    protected ImageView lockScreen, extraMenu, screenRotation, extraControls, onHoldSpeed, previous, next;
    HorizontalScrollView extraControlsLayout;
    private TextView trackName;
    int videoFilesAdapterPosition;
    static String myVFolder;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences1;
    boolean isScreenLocked = false;

    // Audio and Audio-Focus Variables
    private AudioManager audioManager;
    AudioAttributes playbackAttributes;
    boolean extraControlShowing = false;

    // swap & zoom variable
    ConstraintLayout layoutSwapGesture;
    private static final int SWIPE_THRESHOLD = 100;
    private int displayBrightness, maxVolume;
    private boolean isControllerVisible = false;
    private static final float PLAY_SPEED_2X = 2f;
    private static final float PLAY_SPEED_4X = 4f;
    private static final float PLAY_SPEED_6X = 6f;
    private static final float PLAY_SPEED_8X = 8f;
    private static final float PLAY_SPEED_NORMAL = 1f;
    ContentResolver contentResolver;
    private Window window;
    boolean value;
    ConstraintLayout controllerMainLayout;


    // volume and brightness Variable
    private ConstraintLayout volumeLayout, brightnessLayout;
    private ImageView imageViewVolume, imageViewBrightness;
    private ProgressBar progressBarVolume, progressBarBrightness, timeBar;
    private TextView txtVolumeText, txBrightnessText, txtFastForwardBackward, txtSpeedDoubleOnLongPress, txtOnHoldSpeed;
    private ImageView playPauseDoubleTap;
    int doubleTapCountLeft = 0;
    int doubleTapCountRight = 0;

    private long lastTouchTime = 0;
    private static final long DOUBLE_TAP_THRESHOLD = 1000; // threshold in milliseconds
    private boolean externalIntent;
    Uri externalVideoUri;
    boolean isSeekable;
    DefaultTimeBar timerBar;

    public VideoPlayActivity() {
        this.mediaItemList = new ArrayList<>();
        this.mVideoModelArrayList = new ArrayList<>();
    }

    @OptIn(markerClass = UnstableApi.class)
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
            uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
            Log.d(TAG, "onCheckedChanged: " + uiModeManager.getCurrentModeType());
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        EdgeToEdge.enable(this);
        binding = ActivityVideoPlayBinding.inflate(getLayoutInflater());
        final ConstraintLayout root = binding.getRoot();
        setContentView(root);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        initializeUI();

        // Initialize SharedPreferences
        preferences = getSharedPreferences(MY_SHARED_PREFS_VIDEO, MODE_PRIVATE);
        editor = preferences.edit();

        preferences1 = getSharedPreferences(getPackageName() + "Change Playing Speed", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = preferences1.edit();

        // Initialize AudioManager and AudioAttributes
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        playbackAttributes = new AudioAttributes.Builder().build();

        // Initialize ExoPlayer
        initializeExoPlayer();

        // Getting Intent for Playing Video
        handleIntentData();

        // Initialize Speed Control on Long Press
        float speed = preferences1.getFloat("Speed", PLAY_SPEED_2X);
        String textSpeedOnPress = getTextSpeedOnPress(speed);
        txtOnHoldSpeed.setText(textSpeedOnPress);
        new SpeedControl(onHoldSpeed, txtOnHoldSpeed, editor1);

        // Activity Result Launcher for Request Write Settings Permission
        final ActivityResultLauncher<Intent> settingsLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), _ -> {
                    if (Settings.System.canWrite(VideoPlayActivity.this)) {
                        //Write Settings Permissions Granted
                        Log.d(TAG, "onActivityResult: Write Settings Permissions Granted");
                    } else {
                        Toast.makeText(VideoPlayActivity.this, "Write Settings Permissions Denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // Setting Auto Fullscreen enabled OR disabled
        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            if (visibility == View.GONE) {
                setFullScreen(true);
                lockScreen.setVisibility(View.INVISIBLE);
                extraControlsLayout.setVisibility(View.GONE);
            } else if (visibility == View.VISIBLE) {
                setFullScreen(false);
                lockScreen.setVisibility(View.VISIBLE);
            }
        });

        // DoubleTap For Play-Pause implementation
        playPauseDoubleTap.setOnClickListener(_ -> {
            if (!isScreenLocked) {
                if (player.isPlaying()) {
                    pauseVideo();
                    playPauseDoubleTap.setImageResource(R.drawable.play);
                } else {
                    playVideo();
                    playPauseDoubleTap.setImageResource(R.drawable.pause);
                }
            } else {
                Snackbar.make(playerView, "Screen is locked! \n You want to Unlock and play/pause the video?", Snackbar.LENGTH_LONG)
                        .setAction("OK", _ -> {
                            isScreenLocked = !isScreenLocked;
                            if (isScreenLocked) {
                                lockScreen.setImageResource(R.drawable.lock);
                            } else {
                                lockScreen.setImageResource(R.drawable.lock_open);
                            }
                            if (player.isPlaying()) {
                                pauseVideo();
                                playPauseDoubleTap.setImageResource(R.drawable.play);
                            } else {
                                playVideo();
                                playPauseDoubleTap.setImageResource(R.drawable.pause);
                            }

                        }).show();
            }
        });

        // Previous and Next ClickListener
        previous.setOnClickListener(_ -> previous());
        next.setOnClickListener(_ -> next());

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
                        txtFastForwardBackward.setVisibility(View.GONE);
                        timeBar.setVisibility(View.GONE);
                        txtSpeedDoubleOnLongPress.setVisibility(View.GONE);
                        break;
                }
                return super.onTouch(view, motionEvent);
            }

            @Override
            public void onScrollTouch(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                assert e1 != null;
                double deltaX = Math.ceil(e2.getX() - e1.getX());
                double deltaY = Math.ceil(e2.getY() - e1.getY());
                double brightnessSpeed = 0.01;
                if (!isScreenLocked) {
                    try {
                        if (Math.abs(deltaX) > Math.abs(deltaY)) {
                            timeBar.setMin(0);
                            timeBar.setMax((int) player.getDuration());
                            // Horizontal swipe for playback control
                            /*

                            Uncomment below code if not get success in non seekable video conversion!!!!!


                             */
                            /*if (isSeekable) {
                                if ((Math.abs(deltaX) > SWIPE_THRESHOLD) && (distanceX < 0)) {
                                    // Right swipe - Fast forward
                                    forWard_10Sec();
                                    txtFastForwardBackward.setVisibility(View.VISIBLE);
                                    timeBar.setVisibility(View.VISIBLE);
                                    txtFastForwardBackward.setText(ConvertSecondToHHMMSSString((int) player.getCurrentPosition()));
                                    timeBar.setProgress((int) player.getCurrentPosition());
                                } else if (((deltaX) < SWIPE_THRESHOLD) && (distanceX > 0)) {
                                    // Left swipe - Rewind
                                    backWard_10Sec();
                                    txtFastForwardBackward.setVisibility(View.VISIBLE);
                                    timeBar.setVisibility(View.VISIBLE);
                                    txtFastForwardBackward.setText(ConvertSecondToHHMMSSString((int) player.getCurrentPosition()));
                                    timeBar.setProgress((int) player.getCurrentPosition());
                                }
                            }*/
                            if ((Math.abs(deltaX) > SWIPE_THRESHOLD) && (distanceX < 0)) {
                                // Right swipe - Fast forward
                                forWard_10Sec();
                                txtFastForwardBackward.setVisibility(View.VISIBLE);
                                timeBar.setVisibility(View.VISIBLE);
                                txtFastForwardBackward.setText(ConvertSecondToHHMMSSString((int) player.getCurrentPosition()));
                                timeBar.setProgress((int) player.getCurrentPosition());
                            } else if (((deltaX) < SWIPE_THRESHOLD) && (distanceX > 0)) {
                                // Left swipe - Rewind
                                backWard_10Sec();
                                txtFastForwardBackward.setVisibility(View.VISIBLE);
                                timeBar.setVisibility(View.VISIBLE);
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
                                    int scrollRange = mainLayout.getHeight();
                                    double deltaYScaled = (deltaY / (double) scrollRange) * 100; // Normalize deltaY to a 0-100 range
                                    maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                    int mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                                    double calVol = mediaVolume + (-deltaYScaled / 100) * maxVolume;
                                    calVol = Math.max(0, Math.min(maxVolume, calVol));
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) calVol, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                                    double volPercentage = (calVol / (double) maxVolume) * 100;
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
                                // Request permission to change brightness and volume
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                // Launch the permission request
                                settingsLauncher.launch(intent);
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
                setFullScreen(true);

                boolean left = e.getX() < (float) mainLayout.getWidth() / 3;
                boolean right = e.getX() > ((float) mainLayout.getWidth() / 3) * 2;
                boolean center = e.getX() > (float) mainLayout.getWidth() / 3 && e.getX() < ((float) mainLayout.getWidth() / 3) * 2;

                if (!isScreenLocked) {
                    timeBar.setMax((int) player.getDuration());
                    if (isSeekable) {
                        if (left) {
                            calculateTimeBetweenTwoDoubleClick();
                            // Left half - For rewind on Double Tap
                            doubleTapCountRight = 0;
                            doubleTapCountLeft++;
                            Log.d(TAG, "onDoubleTouch: " + doubleTapCountLeft);
                            backWard_10Sec();
                            lockScreen.setVisibility(View.GONE);
                            int increment = 0;
                            if (player.getCurrentPosition() != timeBar.getMin()) {
                                increment = doubleTapCountLeft * 10;
                            }
                            txtFastForwardBackward.setVisibility(View.VISIBLE);
                            txtFastForwardBackward.setText(String.format(Locale.getDefault(), "-%d", increment));
                            timeBar.setProgress(increment);
                        } else if (right) {
                            calculateTimeBetweenTwoDoubleClick();
                            // Right half - For Fast Forward on Double Tap
                            doubleTapCountLeft = 0;
                            doubleTapCountRight++;
                            forWard_10Sec();
                            lockScreen.setVisibility(View.GONE);
                            int increment;
                            if (player.getCurrentPosition() != timeBar.getMax()) {
                                increment = doubleTapCountRight * 10;
                                txtFastForwardBackward.setText(String.format(Locale.getDefault(), "+%d", increment));
                                timeBar.setProgress(increment);
                            } else if (player.getCurrentPosition() == timeBar.getMax()) {
                                increment = 0;
                                txtFastForwardBackward.setText(String.format(Locale.getDefault(), "+%d", increment));
                                timeBar.setProgress(increment);
                            }
                            txtFastForwardBackward.setVisibility(View.VISIBLE);

                        }
                    }
                    if (center) {
                        if (player.isPlaying()) {
                            pauseVideo();
                            playPauseDoubleTap.setImageResource(R.drawable.play);
                        } else {
                            playVideo();
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
                        float speed = preferences1.getFloat("Speed", PLAY_SPEED_2X);
                        player.setPlaybackSpeed(speed);
                        String textSpeedOnPress = getTextSpeedOnPress(speed);
                        txtSpeedDoubleOnLongPress.setText(MessageFormat.format("Playing {0}", textSpeedOnPress));
                        txtSpeedDoubleOnLongPress.setVisibility(View.VISIBLE);

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

        lockScreen.setOnClickListener(_ -> {
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

        extraMenu.setOnClickListener(_ -> {
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
                    String mimeType;
                    try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                        retriever.setDataSource(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath());
                        height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                        width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                        mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);

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
                            + "Video Mime Type:  " + mimeType + "\n\n"
                            + "Size:  " + Formatter.formatFileSize(VideoPlayActivity.this, mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getSize()) + "\n\n"
                            + "Duration:  " + ConvertSecondToHHMMSSString(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getDuration()) + "\n\n"
                            + "Date Added Or Modified: \n" + "\t\t\t\t\t\t" + formattedDate + "\n\n");
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog1, _) -> {
                        playVideo();
                        dialog1.dismiss();
                    });
                    dialog.show();
                }
                return true;
            });
            popupMenu.show();
        });

        extraControls.setOnClickListener(_ -> {
            extraControlShowing = !extraControlShowing;
            if (extraControlShowing) {
                extraControlsLayout.setVisibility(View.VISIBLE);
            } else {
                extraControlsLayout.setVisibility(View.GONE);
            }
        });

        if (!externalIntent) {
            player.addListener(new Player.Listener() {
                @Override
                public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                    Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        savePreference(mVideoModelArrayList.get(player.getCurrentMediaItemIndex() - 1).getPath(), -1);
                    }
                    trackName.setText(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getName());
                }

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    Player.Listener.super.onPlaybackStateChanged(playbackState);
                    if (playbackState == Player.STATE_READY) {
                        isSeekable = player.isCurrentMediaItemSeekable();
                        Log.d("VideoCheck", "Seekable: " + isSeekable);

                        if (!isSeekable) {
                            MediaItem mediaItem = MediaItem.fromUri(mVideoModelArrayList.get(videoFilesAdapterPosition).getPath());
                            EditedMediaItem editedMediaItem = new EditedMediaItem.Builder(mediaItem).build();
                            File file = new File(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath());
                            String fileName = file.getName();
                            File outputFile = new File(getApplicationContext().getFilesDir(), fileName);
                            String outPutFilePath = outputFile.getAbsolutePath();
                            Transformer transformer = new Transformer.Builder(VideoPlayActivity.this)
                                    .setEncoderFactory(new DefaultEncoderFactory.Builder(getApplicationContext()).build())
                                    .setMuxerFactory(new DefaultMuxer.Factory())
                                    .build();
                            transformer.start(editedMediaItem, outPutFilePath);
                            transformer.addListener(new Transformer.Listener() {
                                @Override
                                public void onCompleted(Composition composition, ExportResult exportResult) {
                                    Transformer.Listener.super.onCompleted(composition, exportResult);
                                    Toast.makeText(VideoPlayActivity.this, "Video converted successfully", Toast.LENGTH_SHORT).show();
                                    MediaItem mediaItem = MediaItem.fromUri(outPutFilePath);
                                    player.setMediaItem(mediaItem);
                                    player.prepare();
                                    isSeekable = false;

                                }
                                @SuppressLint("ClickableViewAccessibility")
                                @Override
                                public void onError(Composition composition, ExportResult exportResult, ExportException exportException) {
                                    Transformer.Listener.super.onError(composition, exportResult, exportException);
                                    /*MediaItem mediaItem = MediaItem.fromUri(mVideoModelArrayList.get(videoFilesAdapterPosition).getPath());
                                    player.setMediaItem(mediaItem);
                                    player.prepare();
                                    isSeekable = true;*/
                                    showSnackError("Error: " + exportException.getMessage());
                                    //timerBar.setOnTouchListener((_, _) -> false);
                                }
                            });
                        }
                    }
                    if (playbackState == Player.STATE_ENDED) {
                        savePreference(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath(), -1);
                        player.stop();
                        player.release();
                        player = null;
                        Intent intent = new Intent(VideoPlayActivity.this, VideoFilesActivity.class);
                        intent.putExtra("Folder Name", myVFolder);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                    Toast.makeText(VideoPlayActivity.this, Objects.requireNonNull(error.getCause()).getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        screenRotation.setOnClickListener(_ -> changeOrientation());

        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                try {
                    if (externalIntent) {
                        pauseVideo();
                        player.stop();
                        player.release();
                        finish();
                    } else {
                        if (player.isPlaying()) {
                            pauseVideo();
                            savePreference(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath(), player.getCurrentPosition());
                            player.stop();
                        }
                        player.release();
                        mediaItemList.clear();
                        Intent intent = backspaceIntent();
                        startActivity(intent);
                        finish();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    @NonNull
    private Intent backspaceIntent() {
        Intent intent;
        if (myVFolder.equals("NO Folder Name")) {
            intent = new Intent(VideoPlayActivity.this, MainActivity.class);
        } else {
            intent = new Intent(VideoPlayActivity.this, VideoFilesActivity.class);
            intent.putExtra("Folder Name", myVFolder);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    private void showSnackError(String error){

        Snackbar snackbar = Snackbar.make(mainLayout, error, Snackbar.LENGTH_INDEFINITE);
        snackbar.setBackgroundTint(Color.RED);
        snackbar.setTextColor(Color.BLACK);
        snackbar.setTextMaxLines(10);
        snackbar.setAction("Cancel", _ -> {
            snackbar.dismiss();
        });
        snackbar.show();
    }

    @NonNull
    private static String getTextSpeedOnPress(float speed) {
        String textSpeedOnPress = "2x";
        if (speed == PLAY_SPEED_2X) textSpeedOnPress = "2x";
        else if (speed == PLAY_SPEED_4X) textSpeedOnPress = "4x";
        else if (speed == PLAY_SPEED_6X) textSpeedOnPress = "6x";
        else if (speed == PLAY_SPEED_8X) textSpeedOnPress = "8x";
        return MessageFormat.format("{0} speed", textSpeedOnPress);
    }

    private void initializeUI() {
        playerView = binding.videoView;
        mainLayout = binding.main;
        trackName = playerView.findViewById(R.id.exo_main_text);
        previous = playerView.findViewById(R.id.exo_prev);
        next = playerView.findViewById(R.id.exo_next);
        lockScreen = binding.lockScreen;
        screenRotation = playerView.findViewById(R.id.screen_rotation);
        extraControls = playerView.findViewById(R.id.extra_controls);
        extraControlsLayout = playerView.findViewById(R.id.extra_controls_layout);
        layoutSwapGesture = mainLayout.findViewById(R.id.layout_swap_gesture);
        controllerMainLayout = playerView.findViewById(R.id.layout_player_controller);
        volumeLayout = layoutSwapGesture.findViewById(R.id.layout_swap_gesture_volume);
        brightnessLayout = layoutSwapGesture.findViewById(R.id.layout_swap_gesture_brightness);
        txtFastForwardBackward = layoutSwapGesture.findViewById(R.id.txtfast_forward_backward_position);
        timeBar = layoutSwapGesture.findViewById(R.id.timeBar);
        txtSpeedDoubleOnLongPress = layoutSwapGesture.findViewById(R.id.txt_speed_double_onLong_press);
        imageViewVolume = volumeLayout.findViewById(R.id.imageViewVolume);
        imageViewBrightness = brightnessLayout.findViewById(R.id.imageViewBrightness);
        progressBarVolume = volumeLayout.findViewById(R.id.progressBarVolume);
        progressBarBrightness = brightnessLayout.findViewById(R.id.progressBarBrightness);
        txtVolumeText = volumeLayout.findViewById(R.id.txtVolumeText);
        txBrightnessText = brightnessLayout.findViewById(R.id.txBrightnessText);
        playPauseDoubleTap = playerView.findViewById(R.id.imageViewPlayPauseDoubleTap);
        extraMenu = playerView.findViewById(R.id.setting_list_Menu);
        timerBar = playerView.findViewById(R.id.exo_progress_placeholder);
        onHoldSpeed = playerView.findViewById(R.id.press_hold_forward_speed);
        txtOnHoldSpeed = findViewById(R.id.txt_speed_onLong_press);
    }

    private void handleIntentData() {
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();
        // Getting External Intent Data
        if ("android.intent.action.VIEW".equals(action) ||
                (Objects.equals(type, "audio/*") || Objects.equals(type, "video/*"))) {
            externalIntent = true;
            externalVideoUri = intent.getData();
            assert externalVideoUri != null;
            Log.e("URI", externalVideoUri.toString());
            playBroadcastAudio(externalVideoUri);
            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                    Log.d(TAG, Objects.requireNonNull(error.getMessage()));
                    Toast.makeText(VideoPlayActivity.this, Objects.requireNonNull(error.getCause()).getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Getting Internal Intent Data
            externalIntent = false;
            final Intent intent1 = getIntent();
            videoFilesAdapterPosition = intent1.getIntExtra("position", 0);
            mVideoModelArrayList = intent1.getParcelableArrayListExtra("Parcelable");
            myVFolder = intent1.getStringExtra("Folder Name");
            for (int i = 0; i < mVideoModelArrayList.size(); ++i) {
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(mVideoModelArrayList.get(i).getPath()));
                mediaItemList.add(mediaItem);
            }
            player.setMediaItems(mediaItemList);
            playerView.setPlayer(player);
            player.prepare();
            playVideo1(videoFilesAdapterPosition);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializeExoPlayer() {
        RenderersFactory renderersFactory
                = new DefaultRenderersFactory(this)
                .setEnableDecoderFallback(true)
                .setMediaCodecSelector(MediaCodecSelector.DEFAULT)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
        DefaultExtractorsFactory extractorsFactory
                = new DefaultExtractorsFactory()
                .setConstantBitrateSeekingEnabled(true);
        DefaultMediaSourceFactory mediaSourceFactory
                = new DefaultMediaSourceFactory(this)
                .setDataSourceFactory(new DefaultDataSource.Factory(this));


        player = new ExoPlayer.Builder(this, renderersFactory)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this, extractorsFactory))
                .setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(new DefaultTrackSelector(VideoPlayActivity.this))
                .setHandleAudioBecomingNoisy(true)
                .setAudioAttributes(playbackAttributes, true)
                .setSeekParameters(SeekParameters.CLOSEST_SYNC)
                .build();
    }

    private void playVideo1(int currentPosition) {
        try {
            if (preferences != null && preferences.contains(mVideoModelArrayList.get(currentPosition).getPath())) {
                long position1 = preferences.getLong(mVideoModelArrayList.get(currentPosition).getPath(), 0);
                if (position1 != player.getDuration()) {
                    player.seekTo(currentPosition, position1);
                } else {
                    player.seekTo(currentPosition, 0);
                }
            } else {
                player.seekTo(currentPosition, 0);
            }
            trackName.setText(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getName());
            playVideo();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void previous() {
        player.pause();
        savePreference(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath(), player.getCurrentPosition());
        int position = player.getCurrentMediaItemIndex();
        position--;
        if (position < 0) {
            position = 0;
        }
        playVideo1(position);
    }

    private void next() {
        player.pause();
        savePreference(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath(), player.getCurrentPosition());
        int position = player.getCurrentMediaItemIndex();
        position++;
        if (position > mVideoModelArrayList.size() - 1) {
            position = mVideoModelArrayList.size() - 1;
        }
        playVideo1(position);

    }

    private void playVideo() {
        player.setPlayWhenReady(true);
    }

    private void pauseVideo() {
        player.pause();
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

    private void playBroadcastAudio(Uri uri) {
        MediaItem mediaItem = null;
        if (uri != null) {
            mediaItem = MediaItem.fromUri(uri);
            trackName.setText(uri.getPath());
        } else {
            Log.e("Error External", String.valueOf((Object) null));
        }
        assert mediaItem != null;
        player.setMediaItem(mediaItem);
        playerView.setPlayer(player);
        extraMenu.setVisibility(View.GONE);
        player.prepare();
        playVideo();
    }

    private void calculateTimeBetweenTwoDoubleClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTouchTime > DOUBLE_TAP_THRESHOLD) {
            doubleTapCountLeft = 0;
            doubleTapCountRight = 0;
        }
        lastTouchTime = currentTime;
    }

    @OptIn(markerClass = UnstableApi.class)
    private void hideControllerSwipe() {
        controllerMainLayout.setVisibility(View.GONE);
        lockScreen.setVisibility(View.GONE);
        playerView.hideController();
        setFullScreen(true);
    }

    private void savePreference(String keyAsMediaIndex, long currentPlayedTime) {
        editor.putLong(keyAsMediaIndex, currentPlayedTime);
        editor.commit();
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

    @SuppressLint("SourceLockedOrientationActivity")
    private void changeOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onPause() {
        if (player != null) {
            if (player.isPlaying()) {
                pauseVideo();
                savePreference(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath(), player.getCurrentPosition());
            } else {
                savePreference(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath(), -1);
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            if (!player.isPlaying()) {
                playVideo();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            if (player.isPlaying()) {
                pauseVideo();
                savePreference(mVideoModelArrayList.get(player.getCurrentMediaItemIndex()).getPath(), player.getCurrentPosition());
                player.stop();
                player.release();
                player = null;
            }
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data,
                                 @NonNull ComponentCaller caller) {
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
