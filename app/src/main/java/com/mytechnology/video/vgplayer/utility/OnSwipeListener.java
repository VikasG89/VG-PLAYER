package com.mytechnology.video.vgplayer.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnSwipeListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnSwipeListener(Context c) {
        gestureDetector = new GestureDetector(c, new GestureListener());
    }


    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }


    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            onLongTouch(e);
            super.onLongPress(e);
        }

        @Override
        public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            onDoubleTouch(e);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            onScrollTouch(e1, e2, distanceX, distanceY);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            onSingleTouch();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

    }

    public void onDoubleTouch(MotionEvent e){
    }
    public void onSingleTouch(){
    }
    public void onLongTouch(MotionEvent e){
    }
    public void onScrollTouch(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY){

    }
}