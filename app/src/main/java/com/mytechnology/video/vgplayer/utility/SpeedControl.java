package com.mytechnology.video.vgplayer.utility;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import com.mytechnology.video.vgplayer.R;

public class SpeedControl {

    private static final float PLAY_SPEED_2X = 2.0f;
    private static final float PLAY_SPEED_4X = 4.0f;
    private static final float PLAY_SPEED_6X = 6.0f;
    private static final float PLAY_SPEED_8X = 8.0f;

    private final View onHoldSpeed;
    private final TextView txtOnHoldSpeed;
    private final SharedPreferences.Editor editor;
    private final int[] speedTextResources = {R.string.speed_2x, R.string.speed_4x, R.string.speed_6x, R.string.speed_8x};
    private final float[] speedValues = {PLAY_SPEED_2X, PLAY_SPEED_4X, PLAY_SPEED_6X, PLAY_SPEED_8X};
    private int currentSpeedIndex = 0;

    public SpeedControl(View onHoldSpeed, TextView txtOnHoldSpeed, SharedPreferences.Editor editor) {
        this.onHoldSpeed = onHoldSpeed;
        this.txtOnHoldSpeed = txtOnHoldSpeed;
        this.editor = editor;
        setupClickListener();
    }

    private void setupClickListener() {
        onHoldSpeed.setOnClickListener(_ -> {
            currentSpeedIndex = (currentSpeedIndex + 1) % speedValues.length;
            updateSpeed();
        });
    }

    private void updateSpeed() {
        float currentSpeed = speedValues[currentSpeedIndex];
        int currentSpeedTextResource = speedTextResources[currentSpeedIndex];

        editor.putFloat("Speed", currentSpeed);
        editor.apply();
        txtOnHoldSpeed.setText(currentSpeedTextResource);
    }
}
