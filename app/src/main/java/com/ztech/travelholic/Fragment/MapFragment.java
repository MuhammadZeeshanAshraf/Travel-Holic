package com.ztech.travelholic.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.Listener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.robertlevonyan.views.chip.Chip;
import com.squareup.picasso.Picasso;
import com.ztech.travelholic.Activities.AddLocationActivity;
import com.ztech.travelholic.Activities.LocationDetailActivity;
import com.ztech.travelholic.Activities.ReviewActivity;
import com.ztech.travelholic.Model.User;
import com.ztech.travelholic.R;
import com.ztech.travelholic.Utils.CommonFunctions;
import com.ztech.travelholic.Utils.LocationTrack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class MapFragment extends Fragment implements Listener {
    EasyWayLocation easyWayLocation;
    LocationRequest request;
    LocationTrack locationTrack;
    double currentLat = 0.0;
    double currentLong = 0.0;
    GoogleMap mMap;
    EditText searchBox;
    ImageView search;
    Button addLocation;
    DatabaseReference rootReference;
    AlertDialog waitingDialog;
    ArrayList<com.ztech.travelholic.Model.Location> allList = new ArrayList();
    ArrayList<com.ztech.travelholic.Model.Location> valleyList = new ArrayList();
    ArrayList<com.ztech.travelholic.Model.Location> lakeList = new ArrayList();
    ArrayList<com.ztech.travelholic.Model.Location> waterFallList = new ArrayList();
    ArrayList<com.ztech.travelholic.Model.Location> mountainList = new ArrayList();
    ArrayList<com.ztech.travelholic.Model.Location> fortList = new ArrayList();
    ArrayList<com.ztech.travelholic.Model.Location> parkList = new ArrayList();
    ArrayList<com.ztech.travelholic.Model.Location> tracksList = new ArrayList();
    ArrayList<com.ztech.travelholic.Model.Location> desertList = new ArrayList();
    com.google.android.material.bottomsheet.BottomSheetDialog BottomSheetDialog;
    Chip valley, lake, waterFall, mountain, fort, park, tracks, desert, all;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        rootReference = FirebaseDatabase.getInstance().getReference();
        request = new LocationRequest();
        request.setInterval(50000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        easyWayLocation = new EasyWayLocation(getContext(), request, false, this);

        BottomSheetDialog = new BottomSheetDialog(getContext());
        searchBox = view.findViewById(R.id.fz_search_here);
        search = view.findViewById(R.id.fz_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = searchBox.getText().toString().trim();
                searchBox.setText("");
                com.ztech.travelholic.Model.Location location = null;
                for (com.ztech.travelholic.Model.Location l : allList) {
                    if (l.getTitle().equalsIgnoreCase(address)) {
                        location = l;
                    }
                }
                if (location == null) {
                    SearchAnyPlace(address);
                } else {
                    mMap.clear();
                    LatLng current = new LatLng(Double.parseDouble(location.getLan()), Double.parseDouble(location.getLon()));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(current);
                    markerOptions.icon(getMarkerIcon(location.getCategory()));
                    markerOptions.title(location.getTitle());
                    mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }


            }
        });
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String address = searchBox.getText().toString().trim();
                    searchBox.setText("");
                    com.ztech.travelholic.Model.Location location = null;
                    for (com.ztech.travelholic.Model.Location l : allList) {
                        if (l.getTitle().equalsIgnoreCase(address)) {
                            location = l;
                        }
                    }
                    if (location == null) {
                        SearchAnyPlace(address);
                    } else {
                        mMap.clear();
                        LatLng current = new LatLng(Double.parseDouble(location.getLan()), Double.parseDouble(location.getLon()));
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(current);
                        markerOptions.icon(getMarkerIcon(location.getCategory()));
                        markerOptions.title(location.getTitle());
                        mMap.addMarker(markerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    }

                    return true;
                }
                return false;
            }
        });
        addLocation = view.findViewById(R.id.addLocation);
        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddLocationActivity.class);
                startActivity(intent);
            }
        });

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_view);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(getContext());
                mMap = googleMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                locationTrack = new LocationTrack(getContext());
                if (locationTrack.canGetLocation()) {
                    double longitude = locationTrack.getLongitude();
                    double latitude = locationTrack.getLatitude();
                    currentLong = longitude;
                    currentLat = latitude;
                    LatLng current = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(current).
                            icon(BitmapDescriptorFactory.fromBitmap(
                                    createCustomMarker(getContext(), R.drawable.person_picture, "Current Location")))).setTitle("Current Location");
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 0));
                    Handler handlerr = new Handler();
                    handlerr.postDelayed(new Runnable() {
                        public void run() {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 10));
                        }
                    }, 500);
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            for (int i = 0; i < allList.size(); i++) {
                                if (marker.getTitle().equals(allList.get(i).getTitle())) {
                                    bottomNavigation(allList.get(i));
                                }
                            }
                            return false;
                        }
                    });
                } else {
                    locationTrack.showSettingsAlert();
                }
                getUserAllLocations();
            }
        });

        valley = view.findViewById(R.id.chipValley);
        lake = view.findViewById(R.id.chipLake);
        waterFall = view.findViewById(R.id.chipWaterfall);
        mountain = view.findViewById(R.id.chipMountain);
        fort = view.findViewById(R.id.chipFort);
        park = view.findViewById(R.id.chipNationPark);
        tracks = view.findViewById(R.id.chipHikingTracks);
        desert = view.findViewById(R.id.chipDesert);
        all = view.findViewById(R.id.chipAll);

        valley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationByCategory("Valley");
            }
        });
        lake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationByCategory("Lake");
            }
        });
        waterFall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationByCategory("Waterfall");
            }
        });
        mountain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationByCategory("Mountain");
            }
        });
        fort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationByCategory("Fort");
            }
        });
        park.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationByCategory("Nation Park");
            }
        });
        tracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationByCategory("Hiking Tracks");
            }
        });
        desert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationByCategory("Desert");
            }
        });
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationByCategory("All");
            }
        });
        return view;
    }

    private void getLocationByCategory(String category) {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(5));
        ArrayList<com.ztech.travelholic.Model.Location> requiredList = new ArrayList();
        if (category.equals("Lake")) {
            requiredList = lakeList;
        } else if (category.equals("Waterfall")) {
            requiredList = waterFallList;
        } else if (category.equals("Mountain")) {
            requiredList = mountainList;
        } else if (category.equals("Fort")) {
            requiredList = fortList;
        } else if (category.equals("Desert")) {
            requiredList = desertList;
        } else if (category.equals("Nation Park")) {
            requiredList = parkList;
        } else if (category.equals("Hiking Tracks")) {
            requiredList = tracksList;
        } else if (category.equals("Valley")) {
            requiredList = valleyList;
        } else {
            requiredList = allList;
        }
        mMap.clear();
        for (com.ztech.travelholic.Model.Location location :
                requiredList) {
            LatLng current = new LatLng(Double.parseDouble(location.getLan()), Double.parseDouble(location.getLon()));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(current);
            markerOptions.icon(getMarkerIcon(location.getCategory()));
            markerOptions.title(location.getTitle());
            mMap.addMarker(markerOptions);
        }
    }

    private void getUserAllLocations() {
        lakeList.clear();
        waterFallList.clear();
        mountainList.clear();
        fortList.clear();
        desertList.clear();
        parkList.clear();
        tracksList.clear();
        valleyList.clear();
        allList.clear();
        showLoadingDialog();
        rootReference.child("Locations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String key = snapshot.getKey();
                    if (key != null) {
                        com.ztech.travelholic.Model.Location location = snapshot.getValue(com.ztech.travelholic.Model.Location.class);
                        LatLng current = new LatLng(Double.parseDouble(location.getLan()), Double.parseDouble(location.getLon()));
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(current);
                        markerOptions.icon(getMarkerIcon(location.getCategory()));
                        markerOptions.title(location.getTitle());
                        mMap.addMarker(markerOptions);
                        allList.add(location);
                        addLocationToRelevantCategory(location);
                    }
                }
                waitingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (CommonFunctions.isNetworkAvailable(getContext())) {
            rootReference.child("AppLocations").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final String key = snapshot.getKey();
                        if (key != null) {
                            com.ztech.travelholic.Model.Location location = snapshot.getValue(com.ztech.travelholic.Model.Location.class);
                            LatLng current = new LatLng(Double.parseDouble(location.getLan()), Double.parseDouble(location.getLon()));
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(current);
                            markerOptions.icon(getMarkerIcon(location.getCategory()));
                            markerOptions.title(location.getTitle());
                            mMap.addMarker(markerOptions);
                            allList.add(location);
                            addLocationToRelevantCategory(location);
                        }
                    }
                    waitingDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            String appLocations = getLocationsString();
            JSONObject resObj = null;
            try {
                resObj = new JSONObject(appLocations);
                Iterator<?> keys = resObj.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    System.out.println(key);
                    if (resObj.get(key) instanceof JSONObject) {
                        JSONObject xx = new JSONObject(resObj.get(key).toString());
                        String category = xx.getString("Category");
                        String description = xx.getString("Description");
                        String ID = xx.getString("ID");
                        String lan = xx.getString("Lan");
                        String lon = xx.getString("Lon");
                        String rate = xx.getString("Rate");
                        String title = xx.getString("Title");
                        String uri = xx.getString("Uri");
                        com.ztech.travelholic.Model.Location location = new com.ztech.travelholic.Model.Location(
                                category, description, ID, lan, lon, Long.parseLong(rate), title, uri
                        );
                        LatLng current = new LatLng(Double.parseDouble(location.getLan()), Double.parseDouble(location.getLon()));
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(current);
                        markerOptions.icon(getMarkerIcon(location.getCategory()));
                        markerOptions.title(location.getTitle());
                        mMap.addMarker(markerOptions);
                        allList.add(location);
                        addLocationToRelevantCategory(location);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public BitmapDescriptor getMarkerIcon(String category) {
        int color = getResources().getColor(R.color.colorPrimary);
        if (category.equals("Lake")) {
            color = getResources().getColor(R.color.red);
        } else if (category.equals("Waterfall")) {
            color = getResources().getColor(R.color.orange_active);
        } else if (category.equals("Mountain")) {
            color = getResources().getColor(R.color.yellow_active);
        } else if (category.equals("Fort")) {
            color = getResources().getColor(R.color.blue_active);
        } else if (category.equals("Desert")) {
            color = getResources().getColor(R.color.midnigth_blue);
        } else if (category.equals("Nation Park")) {
            color = getResources().getColor(R.color.av_color5);
        } else if (category.equals("Hiking Tracks")) {
            color = getResources().getColor(R.color.purple_active);
        }

        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    private void addLocationToRelevantCategory(com.ztech.travelholic.Model.Location location) {
        if (location.getCategory().equals("Lake")) {
            lakeList.add(location);
        } else if (location.getCategory().equals("Waterfall")) {
            waterFallList.add(location);
        } else if (location.getCategory().equals("Mountain")) {
            mountainList.add(location);
        } else if (location.getCategory().equals("Fort")) {
            fortList.add(location);
        } else if (location.getCategory().equals("Desert")) {
            desertList.add(location);
        } else if (location.getCategory().equals("Nation Park")) {
            parkList.add(location);
        } else if (location.getCategory().equals("Hiking Tracks")) {
            tracksList.add(location);
        } else {
            valleyList.add(location);
        }
    }

    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String _name) {
        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);
        TextView txt_name = (TextView) marker.findViewById(R.id.name);
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

    @Override
    public void locationOn() {

    }

    @Override
    public void currentLocation(Location location) {
//        mMap.clear();
//        double longitude = location.getLongitude();
//        double latitude = location.getLatitude();
//        currentLong = longitude;
//        currentLat = latitude;
//        LatLng current = new LatLng(latitude, longitude);
//        mMap.addMarker(new MarkerOptions().position(current).
//                icon(BitmapDescriptorFactory.fromBitmap(
//                        createCustomMarker(getContext(), R.drawable.person_picture, "Current Location")))).setTitle("Current Location");
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 0));
//        Handler handlerr = new Handler();
//        handlerr.postDelayed(new Runnable() {
//            public void run() {
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 10));
//            }
//        }, 500);
//        easyWayLocation.endUpdates();
    }

    @Override
    public void locationCancelled() {

    }

    @Override
    public void onResume() {
        super.onResume();
        easyWayLocation.startLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        easyWayLocation.endUpdates();
    }

    private void SearchAnyPlace(String address) {
        List<Address> addressList = null;
        MarkerOptions userMarkerOptions = new MarkerOptions();
        if (!TextUtils.isEmpty(address)) {
            Geocoder geocoder = new Geocoder(getContext());
            try {
                addressList = geocoder.getFromLocationName(address, 6);
                if (addressList != null) {
                    for (int i = 0; i < addressList.size(); i++) {
                        Address userAddress = addressList.get(i);
                        LatLng latLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());
                        userMarkerOptions.position(latLng);
                        userMarkerOptions.title(address);
                        userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        mMap.addMarker(userMarkerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                    }
                } else {
                    Toast.makeText(getContext(), "Location Not Found....", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "Please write any location name ....", Toast.LENGTH_SHORT).show();
        }

    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_please_wait, null, false);
        builder.setView(view);
        waitingDialog = builder.create();
        waitingDialog.setCanceledOnTouchOutside(false);
        waitingDialog.setCancelable(false);
        waitingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        waitingDialog.show();
    }


    private void bottomNavigation(com.ztech.travelholic.Model.Location location) {
        RoundedImageView imageView;
        ImageView fav;
        TextView title, category;
        RatingBar rate;
        Button review;

        View view = getLayoutInflater().inflate(R.layout.custom_bottom_sheet, null);
        imageView = view.findViewById(R.id.locationImage);
        title = view.findViewById(R.id.title);
        category = view.findViewById(R.id.category);
        rate = view.findViewById(R.id.locationRating);
        review = view.findViewById(R.id.review);

        String str = location.getTitle();
        str = str.replaceAll("[^a-zA-Z0-9]", "");
        str = str.replace(" ", "");
        str = str.replaceAll("[\\[\\](){}]","");
        str = str.toLowerCase();

        if (location.getUri().isEmpty()) {

            int identifier = getResources().getIdentifier(str, "drawable", getContext().getPackageName());
            if (identifier != 0) {
                @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(getResources()
                        .getIdentifier(str, "drawable", getContext().getPackageName()));
                imageView.setBackground(drawable);
            } else {
                imageView.setImageResource(R.drawable.duikar);
            }

        } else {
            Picasso.get().load(location.getUri()).placeholder(R.drawable.duikar).error(R.drawable.duikar).into(imageView);
        }
        title.setText(location.getTitle());
        category.setText("Category : " + location.getCategory());
        rate.setRating(location.getRate());
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), ReviewActivity.class);
                i.putExtra("Location", location);
                getContext().startActivity(i);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), LocationDetailActivity.class);
                intent.putExtra("Location", location);
                getContext().startActivity(intent);
            }
        });
        BottomSheetDialog.setContentView(view);
        BottomSheetDialog.setCanceledOnTouchOutside(true);
        BottomSheetDialog.show();
    }

    public String getLocationsString() {
        StringBuilder termsString = new StringBuilder();
        BufferedReader reader;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getResources().getAssets().open("locations.txt")));

            String str;
            while ((str = reader.readLine()) != null) {
                termsString.append(str);
            }

            reader.close();
            return termsString.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}