package com.ztech.travelholic.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.ztech.travelholic.Adapter.MangeAlbumAdapter;
import com.ztech.travelholic.Model.Album;
import com.ztech.travelholic.Model.User;
import com.ztech.travelholic.R;
import com.ztech.travelholic.databinding.ActivityAddAlbumBinding;
import com.ztech.travelholic.databinding.ActivityAlbumistProfileBinding;

import java.util.ArrayList;

public class AlbumistProfileActivity extends AppCompatActivity {
    ActivityAlbumistProfileBinding binding;
    User model;
    Intent productIntent;
    DatabaseReference rootReference;
    ArrayList<Album> list;
    MangeAlbumAdapter adapter;
    AlertDialog waitingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = ActivityAlbumistProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        productIntent = getIntent();
        model = (User) productIntent.getSerializableExtra("User");

        binding.username.setText(model.getUsername());
        binding.email.setText(model.getEmail());
        binding.phone.setText(model.getPhoneNumber());
        binding.address.setText(model.getAddress());
        binding.profession.setText(model.getProfession());
        Picasso.get().load(model.getUri()).placeholder(R.drawable.person_picture).error(R.drawable.person_picture).into(binding.editProfileImage);

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        init();
    }

    private void init() {
        showLoadingDialog();
        rootReference = FirebaseDatabase.getInstance().getReference();
        list = new ArrayList<>();
        getUserAllAlbum();
    }

    private void getUserAllAlbum() {
        rootReference.child("UserAlbums").child(model.getID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String key = snapshot.getKey();
                    if (key != null) {
                        Album album = snapshot.getValue(Album.class);
                        list.add(album);
                        binding.emptyLayout.setVisibility(View.GONE);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(AlbumistProfileActivity.this, LinearLayoutManager.VERTICAL, false);
                        binding.AlbumList.setLayoutManager(layoutManager);
                        binding.AlbumList.setItemAnimator(new DefaultItemAnimator());
                        adapter = new MangeAlbumAdapter(AlbumistProfileActivity.this, list, AlbumistProfileActivity.this,0);
                        binding.AlbumList.setAdapter(adapter);
                    }
                }
                waitingDialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AlbumistProfileActivity.this);
        View view = LayoutInflater.from(AlbumistProfileActivity.this).inflate(R.layout.dialog_please_wait, null, false);
        builder.setView(view);
        waitingDialog = builder.create();
        waitingDialog.setCanceledOnTouchOutside(false);
        waitingDialog.setCancelable(false);
        waitingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        waitingDialog.show();
    }


}