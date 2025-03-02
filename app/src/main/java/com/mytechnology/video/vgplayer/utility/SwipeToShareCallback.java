package com.mytechnology.video.vgplayer.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.mytechnology.video.vgplayer.R;
import com.mytechnology.video.vgplayer.videos.VideoFilesActivity;

public class SwipeToShareCallback extends ItemTouchHelper.SimpleCallback {

    private final Drawable icon;
    private final Paint backgroundPaint;
    Context context;
    private final Object lock;
    private boolean isProcessing ;
    ShareHelper shareHelper;

    public SwipeToShareCallback(Context context, Object lock) {
        super(0, ItemTouchHelper.RIGHT);
        this.context = context;
        this.lock = lock;
        isProcessing = false;
        icon = ContextCompat.getDrawable(context, R.drawable.share_icon); // Your share icon
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.BLUE); // Background color when swiping
        backgroundPaint.setAlpha(128); // Semi-transparent background
        shareHelper = new ShareHelper(context);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false; // We don't support moving items in this example
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Handle the swipe action (e.g., trigger the share action)
        synchronized (lock) {
            isProcessing = true;
            // Perform some processing
            if (direction == ItemTouchHelper.RIGHT) {
                try {
                    Log.d("FilePath", "Selected File: " + VideoFilesActivity.videoModels.get(viewHolder.getBindingAdapterPosition()).getPath());

                    shareHelper.shareVideo(VideoFilesActivity.videoModels.get(viewHolder.getBindingAdapterPosition()).getPath());
                } catch (Exception e) {
                    Toast.makeText(context, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                } finally{
                    isProcessing = false;

                }
            }
            lock.notify();
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        // Draw the background
        int itemViewTop = viewHolder.itemView.getTop();
        int itemViewBottom = viewHolder.itemView.getBottom();
        int itemViewLeft = viewHolder.itemView.getLeft();
        int itemViewRight = viewHolder.itemView.getRight();

        canvas.drawRect(itemViewLeft, itemViewTop, itemViewRight, itemViewBottom, backgroundPaint);

        // Draw the share icon
        int iconMargin = (viewHolder.itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemViewTop + (viewHolder.itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();
        int iconLeft = itemViewLeft + iconMargin;
        int iconRight = iconLeft + icon.getIntrinsicWidth();

        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        icon.draw(canvas);
        getSwipeThreshold(viewHolder);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return super.getSwipeThreshold(viewHolder);
    }
}
