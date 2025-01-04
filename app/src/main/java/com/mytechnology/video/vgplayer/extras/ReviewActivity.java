package com.mytechnology.video.vgplayer.extras;

import static android.content.ContentValues.TAG;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.testing.FakeReviewManager;
import com.mytechnology.video.vgplayer.R;


public class ReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    ReviewManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* SharedPreferences preferences = getSharedPreferences("Change Theme", MODE_PRIVATE);
        changeTheme = preferences.getBoolean("Theme Changed", false);
        if (changeTheme) {
            setTheme(R.style.My_AppTheme_NoActionBar);
        }*/
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        ratingBar = findViewById(R.id.ratingBar);

        // Initialize the ReviewManager
        //manager = ReviewManagerFactory.create(this);

        // Create a fake review manager for testing purposes
        manager = new FakeReviewManager(this);

        // Set up a listener for the "Submit" button
        Button submitButton = findViewById(R.id.btnSubmitReview);
        submitButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating >= 4) {
                // Redirect to Google Play Store
                // Request in-app review
                requestInAppReview();
            } else {
                // Show a thank you message
                Toast.makeText(ReviewActivity.this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestInAppReview() {
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We got the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                launchReviewFlow(reviewInfo);
            } /*else {
                // There was some problem, log or handle the error code.
                *//*@ReviewErrorCode int reviewErrorCode = ((ReviewException) Objects.requireNonNull(task.getException())).getErrorCode();
                Log.e(TAG, "getReviewInfoCode: "+ reviewErrorCode );*//*
            }*/
        });
    }

    private void launchReviewFlow(ReviewInfo reviewInfo) {
        Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
        flow.addOnCompleteListener(task -> {
            // The flow has finished. The API does not indicate whether the user
            // reviewed or not, or even whether the review dialog was shown.
            // Thus, no matter the result, we continue our app flow.
            Log.d(TAG, "launchInAppReview: "+ task);
        });
    }

}