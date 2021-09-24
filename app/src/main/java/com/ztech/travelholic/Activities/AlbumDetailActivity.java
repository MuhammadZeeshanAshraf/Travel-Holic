package com.ztech.travelholic.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.squareup.picasso.Picasso;
import com.ztech.travelholic.Adapter.AddAlbumAdapter;
import com.ztech.travelholic.Model.Album;
import com.ztech.travelholic.R;
import com.ztech.travelholic.databinding.ActivityAddAlbumBinding;
import com.ztech.travelholic.databinding.ActivityAlbumDetailBinding;

public class AlbumDetailActivity extends AppCompatActivity {

    ActivityAlbumDetailBinding binding;
    Intent productIntent;
    Album model;
    AddAlbumAdapter addAlbumAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = ActivityAlbumDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
    }

    private void init()
    {
        productIntent = getIntent();
        model = (Album) productIntent.getSerializableExtra("Album");

        binding.title.setText(model.getTitle());
        binding.dec.setText(model.getDescription());
        binding.tvCategory.setText(model.getCategory());
        binding.creator.setText("Created By : " +model.getUser().getUsername() );

        Picasso.get().load(model.getUri().get(0)).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(binding.Image);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(AlbumDetailActivity.this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setAlignItems(AlignItems.STRETCH);
        binding.imageList.setLayoutManager(layoutManager);
        binding.imageList.setItemAnimator(new DefaultItemAnimator());
        addAlbumAdapter = new AddAlbumAdapter(AlbumDetailActivity.this, model.getUri(), AlbumDetailActivity.this,1);
        binding.imageList.setAdapter(addAlbumAdapter);
    }

    private void setListener()
    {
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.openProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlbumDetailActivity.this, AlbumistProfileActivity.class);
                intent.putExtra("User", model.getUser());
                startActivity(intent);
            }
        });
    }
}