package com.ztech.travelholic.Activities;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.Listener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;
import com.ztech.travelholic.R;
import com.ztech.travelholic.Utils.LocationTrack;

import java.util.ArrayList;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import im.delight.android.location.SimpleLocation;

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback, Listener {

    SimpleLocation location;
    GoogleMap mMap;
    Toolbar locationPrivacyToolBar;
    CircularProgressButton btn;
    EasyWayLocation easyWayLocation;
    LocationRequest request;
    static AlertDialog waitingDialog;
    String lan, lon, routeDistance;
    Double a, b;
    int rate = 10;
    DatabaseReference rootReference;
    FirebaseAuth firebaseAuth;
    String currentUserId;

    ArrayList permissionsToRequest;
    ArrayList permissionsRejected = new ArrayList();
    ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_add_location);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        lan = "null";
        lon = "null";
        initializeField();
        showLoadingDialog();

        request = new LocationRequest();
        request.setInterval(50000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        easyWayLocation = new EasyWayLocation(AddLocationActivity.this, request, false, this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        location = new SimpleLocation(AddLocationActivity.this);
        easyWayLocation.startLocation();
        if (!location.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(AddLocationActivity.this);
        } else {
            easyWayLocation.startLocation();
            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();

        }


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!(lan.equals("null"))) {
                    btn.startAnimation();
                    Intent intent = new Intent(AddLocationActivity.this, AddLocationDetailActivity.class);
                    intent.putExtra("lan", lan);
                    intent.putExtra("lon", lon);
                    startActivity(intent);
                    finish();
                } else {
                    Toasty.info(AddLocationActivity.this, "First Chose some Location on Map", Toasty.LENGTH_SHORT).show();
                }

            }
        });

    }

    private ArrayList findUnAskedPermissions(ArrayList wanted) {
        ArrayList result = new ArrayList();

        for (Object perm : wanted) {
            if (!hasPermission((String) perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (ContextCompat.checkSelfPermission(AddLocationActivity.this, permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (Object perms : permissionsToRequest) {
                    if (!hasPermission((String) perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale((String) permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions((String[]) permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(AddLocationActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }

    public void initializeField() {

        btn = findViewById(R.id.addLocationDetail);
        locationPrivacyToolBar = findViewById(R.id.addLocationTopbar);
        setSupportActionBar(locationPrivacyToolBar);
        locationPrivacyToolBar.setNavigationIcon(R.drawable.ic_arrow_back);
        locationPrivacyToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng current = new LatLng(31.512765, 74.2581817);
//        googleMap.addMarker(new MarkerOptions().position(current).
//                icon(BitmapDescriptorFactory.fromBitmap(
//                        createCustomMarker(AddLocationActivity.this, R.drawable.person_picture, "Location")))).setTitle("Location");

        waitingDialog.dismiss();

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 0));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 10));
            }
        }, 500);


        locationTrack = new LocationTrack(AddLocationActivity.this);


        if (locationTrack.canGetLocation()) {


            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();

//            Toast.makeText(getContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();

            LatLng currentt = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(currentt).
                    icon(BitmapDescriptorFactory.fromBitmap(
                            createCustomMarker(AddLocationActivity.this, R.drawable.person_picture, "Current Location")))).setTitle("Current Location");


            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentt, 0));
            Handler handlerr = new Handler();
            handlerr.postDelayed(new Runnable() {
                public void run() {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentt, 10));
                }
            }, 500);
        } else {

            locationTrack.showSettingsAlert();
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

                mMap.clear();
                lan = "" + point.latitude;
                a = point.latitude;
                lon = "" + point.longitude;
                b = point.longitude;
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(point);
                mMap.addMarker(markerOptions);
            }
        });
    }

    @Override
    public void locationOn() {

    }

    @Override
    public void currentLocation(Location location) {

    }

    @Override
    public void locationCancelled() {

    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //setting up the layout for alert dialog
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_wait, null, false);
        builder.setView(view);
        waitingDialog = builder.create();
        waitingDialog.setCanceledOnTouchOutside(false);
        waitingDialog.setCancelable(false);
        waitingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        waitingDialog.show();
    }

    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String
            _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        CircleImageView markerImage = marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);
        try {
            Picasso.get().load(HomeActivity.HOME_USER.getUri()).placeholder(R.drawable.person_picture).error(R.drawable.person_picture).into(markerImage);

        } catch (Exception e) {
            markerImage.setImageResource(resource);

        }
        TextView txt_name = marker.findViewById(R.id.name);
        txt_name.setText(_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }


}