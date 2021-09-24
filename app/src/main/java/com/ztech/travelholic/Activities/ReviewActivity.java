package com.ztech.travelholic.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.DragEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;

import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.yugansh.tyagi.smileyrating.SmileyRatingView;
import com.ztech.travelholic.Model.Location;
import com.ztech.travelholic.R;
import com.ztech.travelholic.Utils.CommonFunctions;
import com.ztech.travelholic.databinding.ActivityReviewBinding;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ReviewActivity extends AppCompatActivity {
    ActivityReviewBinding binding;
    Intent productIntent;
    Location model;
    RatingBar ratingBar;
    SmileyRatingView smileyRating;
    DatabaseReference rootRef;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    DatabaseReference rootReference;
    String currentUserId;
    int Matrix_Factor = 2;
    String rate = "0.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = ActivityReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        rootRef = FirebaseDatabase.getInstance().getReference();
        Wave wave = new Wave();
        binding.LoadingBar.setIndeterminateDrawable(wave);
        binding.LoadingBar.setVisibility(View.INVISIBLE);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        rootReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = currentUser.getUid();

        productIntent = getIntent();
        model = (Location) productIntent.getSerializableExtra("Location");
        smileyRating = findViewById(R.id.smiley_view);

        ratingBar = findViewById(R.id.rating_bar);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rate = "" + rating;
                smileyRating.setSmiley(rating);
            }
        });

        binding.submitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String review = binding.reviewBox.getText().toString();
                if (CommonFunctions.isNetworkAvailable(ReviewActivity.this)) {
                    if (TextUtils.isEmpty(review)) {
                        binding.reviewBox.setError("Review Required");
                    } else {
                        binding.LoadingBar.setVisibility(View.VISIBLE);
                        DatabaseReference userKeyRef = rootRef.child("Review").push();
                        final String PushID = userKeyRef.getKey();
                        Map MessageMap = new HashMap<>();
                        MessageMap.put("ID", PushID);
                        MessageMap.put("Rating", rate);
                        MessageMap.put("Feedback", review);
                        MessageMap.put("LocationID", model.getID());
                        rootRef.child("Review").child(PushID).updateChildren(MessageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toasty.info(ReviewActivity.this, "Location Review is added Successfully.....", Toasty.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ReviewActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    binding.LoadingBar.setVisibility(View.GONE);
                                    Toasty.error(ReviewActivity.this, "Some Problem happen will adding the Location Review ...!", Toasty.LENGTH_SHORT).show();
                                }
                            }
                        });
                        double r = (model.getRate() + Double.parseDouble(rate)) / Matrix_Factor;
                        Map MessageMapp = new HashMap<>();
                        MessageMapp.put("Rate", r);
                        if (model.getTitle().equals(model.getID())) {
                            rootRef.child("AppLocations").child(model.getID()).updateChildren(MessageMapp);
                        } else {
                            rootRef.child("Locations").child(model.getID()).updateChildren(MessageMapp);
                            rootRef.child("CategoryLocations").child(model.getCategory()).child(model.getID()).updateChildren(MessageMapp);
                        }
                    }
                } else {
                    CommonFunctions.showShortToastInfo(ReviewActivity.this, "Check Your Internet ! Make Sure Your are Connected to Internet ");
                }

            }
        });
    }
}