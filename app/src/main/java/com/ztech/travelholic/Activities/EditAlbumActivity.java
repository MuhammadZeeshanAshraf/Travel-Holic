package com.ztech.travelholic.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.ztech.travelholic.Adapter.AddAlbumAdapter;
import com.ztech.travelholic.Model.Album;
import com.ztech.travelholic.R;
import com.ztech.travelholic.databinding.ActivityEditAlbumBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class EditAlbumActivity extends AppCompatActivity {
    ActivityEditAlbumBinding binding;
    String Category = "Valley";
    DatabaseReference rootRef;
    Uri AlbumUri;
    StorageReference AlbumImageReference;
    String AlbumUriString;
    AlertDialog waitingDialog;
    private final int OPEN_MEDIA_PICKER = 1;
    AddAlbumAdapter addAlbumAdapter;
    ArrayList<String> selectionResult = new ArrayList<>();
    Intent productIntent;
    Album model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = ActivityEditAlbumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        init();
        setListener();
    }

    private void init() {
        productIntent = getIntent();
        model = (Album) productIntent.getSerializableExtra("Album");


        AlbumUri = null;
        rootRef = FirebaseDatabase.getInstance().getReference();
        AlbumImageReference = FirebaseStorage.getInstance().getReference().child("AlbumsImages");
        Wave wave = new Wave();
        binding.LoadingBar.setIndeterminateDrawable(wave);
        binding.LoadingBar.setVisibility(View.INVISIBLE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(EditAlbumActivity.this,
                R.array.category, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.CatSpinner.setAdapter(adapter);
        AlbumUriString = "";

        Category = model.getCategory();
        binding.title.setText(model.getTitle());
        binding.description.setText(model.getDescription());

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(EditAlbumActivity.this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setAlignItems(AlignItems.STRETCH);
        binding.imageList.setLayoutManager(layoutManager);
        binding.imageList.setItemAnimator(new DefaultItemAnimator());
        addAlbumAdapter = new AddAlbumAdapter(EditAlbumActivity.this, model.getUri(), EditAlbumActivity.this, 1);
        binding.imageList.setAdapter(addAlbumAdapter);

    }

    private void setListener() {
        binding.Publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckFields();
            }
        });
        binding.CatSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                Category = item.toString();
            }
        });

    }


    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditAlbumActivity.this);
        //setting up the layout for alert dialog
        View view = LayoutInflater.from(EditAlbumActivity.this).inflate(R.layout.dialog_please_wait, null, false);
        builder.setView(view);
        waitingDialog = builder.create();
        waitingDialog.setCanceledOnTouchOutside(false);
        waitingDialog.setCancelable(false);
        waitingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        waitingDialog.show();
    }

    private void CheckFields() {
        try {

            final String title = binding.title.getText().toString();
            final String desc = binding.description.getText().toString();

            if (TextUtils.isEmpty(title)) {
                binding.title.setError("Title Required");
                Toasty.error(EditAlbumActivity.this, "Enter Album Title", Toasty.LENGTH_SHORT).show();

            } else if (Category.equals("")) {
                Toasty.error(EditAlbumActivity.this, "Select Album Category", Toasty.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(desc)) {
                binding.description.setError("Description Required");
                Toasty.error(EditAlbumActivity.this, "Enter Album Description", Toasty.LENGTH_SHORT).show();

            } else {
                if (!(TextUtils.isEmpty(title))) {
                    if (!(TextUtils.isEmpty(desc))) {
                        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                        ConnectivityManager cm =
                                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        boolean isConnected = activeNetwork != null &&
                                activeNetwork.isConnectedOrConnecting();


                        if (mWifi.isConnected() || isConnected) {
                            AddItemToDatabase(title, desc);

                        } else {
                            Toasty.warning(EditAlbumActivity.this, "Check Your Internet ! Make Sure Your are Connected to Internet ", Toasty.LENGTH_SHORT).show();
                        }
                    }

                }
            }

        } catch (Exception e) {
            binding.LoadingBar.setVisibility(View.GONE);
            if (AlbumUriString.equals("")) {
                Toasty.error(EditAlbumActivity.this, "Select Preview Image of the album item", Toasty.LENGTH_SHORT).show();

            } else {
                Toasty.error(EditAlbumActivity.this, "Some Problem happen will adding the item...!", Toasty.LENGTH_SHORT).show();

            }

        }
    }

    private void AddItemToDatabase(String name, String desc) {
        showLoadingDialog();
        binding.LoadingBar.setVisibility(View.VISIBLE);
        try {
            Map MessageMap = new HashMap<>();
            MessageMap.put("Title", name);
            MessageMap.put("Description", desc);
            MessageMap.put("Category", Category);

            rootRef.child("Albums").child(model.getID()).updateChildren(MessageMap);
            rootRef.child("UserAlbums").child(HomeActivity.HOME_USER.getID()).child(model.getID()).updateChildren(MessageMap);
            rootRef.child("CategoryAlbums").child(model.getCategory()).child(model.getID()).removeValue();
            rootRef.child("CategoryAlbums").child(Category).child(model.getID()).updateChildren(MessageMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    waitingDialog.dismiss();
                    finish();
                }
            });


        } catch (Exception error) {
            error.printStackTrace();
            waitingDialog.dismiss();
            binding.LoadingBar.setVisibility(View.GONE);
            Toasty.error(EditAlbumActivity.this, "Some Problem happen will adding Album...!", Toasty.LENGTH_SHORT).show();
        }

    }
}