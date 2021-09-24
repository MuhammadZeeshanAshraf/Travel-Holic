package com.ztech.travelholic.Fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ztech.travelholic.Adapter.FeaturedAdapter;
import com.ztech.travelholic.Model.Location;
import com.ztech.travelholic.R;
import com.ztech.travelholic.Utils.CommonFunctions;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    RecyclerView featuredRecycler;
    DatabaseReference rootReference;
    AlertDialog waitingDialog;
    ArrayList<Location> allList = new ArrayList();
    FeaturedAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  view =  inflater.inflate(R.layout.fragment_home, container, false);
        featuredRecycler = view.findViewById(R.id.featured_recycler);
        rootReference = FirebaseDatabase.getInstance().getReference();
        getUserAllLocations();
        return  view;
    }

    private void getUserAllLocations() {
        showLoadingDialog();
        if (CommonFunctions.isNetworkAvailable(getContext())) {
            rootReference.child("AppLocations").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final String key = snapshot.getKey();
                        Location location = snapshot.getValue(Location.class);
                        if(location.getRate() > 3.5)
                        {
                            allList.add(location);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                            featuredRecycler.setLayoutManager(layoutManager);
                            featuredRecycler.setItemAnimator(new DefaultItemAnimator());
                            adapter = new FeaturedAdapter(getContext(), allList);
                            featuredRecycler.setAdapter(adapter);
                        }
                    }
                    waitingDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        rootReference.child("Locations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String key = snapshot.getKey();
                    if (key != null) {
                        Location location = snapshot.getValue(Location.class);
                        if(location.getRate() > 4)
                        {
                            allList.add(location);

                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                            featuredRecycler.setLayoutManager(layoutManager);
                            featuredRecycler.setItemAnimator(new DefaultItemAnimator());
                            adapter = new FeaturedAdapter(getContext(), allList);
                            featuredRecycler.setAdapter(adapter);
                        }

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_please_wait, null, false);
        builder.setView(view);
        waitingDialog = builder.create();
        waitingDialog.setCanceledOnTouchOutside(false);
        waitingDialog.setCancelable(false);
        waitingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        waitingDialog.show();
    }
}