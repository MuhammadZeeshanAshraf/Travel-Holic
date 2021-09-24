package com.ztech.travelholic.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;
import com.ztech.travelholic.R;

import java.io.File;

public class PhotoViewerActivity extends AppCompatActivity {
    Intent productIntent;
    ImageView albumImage;
    int checker = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_photo_viewer);
        albumImage = findViewById(R.id.iv_ItemImage);
        productIntent = getIntent();
        String imageUrl = productIntent.getStringExtra("Album");
        checker = productIntent.getIntExtra("checker",0);

        if (checker == 0) {
            Uri uriFromPath = Uri.fromFile(new File(imageUrl));
            Picasso.get().load(uriFromPath).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(albumImage);
        } else {
            Picasso.get().load(Uri.parse(imageUrl)).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(albumImage);
        }

//        Picasso.get().load(Uri.parse(imageUrl)).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(albumImage);
    }


}