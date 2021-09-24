package com.ztech.travelholic.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.ztech.travelholic.Model.Album;
import com.ztech.travelholic.Model.Location;
import com.ztech.travelholic.R;
import com.ztech.travelholic.databinding.ActivityLocationDetailBinding;

import java.util.Locale;

public class LocationDetailActivity extends AppCompatActivity {
    ActivityLocationDetailBinding binding;
    Intent locationIntent;
    Location model;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = ActivityLocationDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
    }

    private void init() {
        locationIntent = getIntent();
        model = (Location) locationIntent.getSerializableExtra("Location");

        binding.title.setText(model.getTitle());
        binding.dec.setText(model.getDescription());
        binding.category.setText(model.getCategory());
        binding.ratingBar.setRating(model.getRate());

        String str = model.getTitle();
        str = str.replaceAll("[^a-zA-Z0-9]", "");
        str = str.replace(" ", "");
        str = str.toLowerCase();

        if (model.getUri().isEmpty()) {
            int identifier = getResources().getIdentifier(str, "drawable", LocationDetailActivity.this.getPackageName());
            if (identifier != 0) {
                @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(getResources()
                        .getIdentifier(str, "drawable", LocationDetailActivity.this.getPackageName()));
                binding.Image.setBackground(drawable);
            } else {
                binding.Image.setImageResource(R.drawable.duikar);
            }
        } else {
            Picasso.get().load(model.getUri()).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(binding.Image);
        }

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                LatLng point = new LatLng(Double.parseDouble(model.getLan()), Double.parseDouble(model.getLon()));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                mMap.addMarker(markerOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 0));
                Handler handlerr = new Handler();
                handlerr.postDelayed(new Runnable() {
                    public void run() {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10));
                    }
                }, 500);
            }
        });
    }

    private void setListener() {
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.openGoogleMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "http://maps.google.com/maps?daddr=" + model.getLan() + "," + model.getLon() + " (" + model.getTitle() + ")";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    try {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(unrestrictedIntent);
                    } catch (ActivityNotFoundException innerEx) {
                        Toast.makeText(LocationDetailActivity.this, "Please install a maps application", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

}