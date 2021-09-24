package com.ztech.travelholic.Fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.ztech.travelholic.Activities.AddAlbumActivity;
import com.ztech.travelholic.Activities.AlbumistProfileActivity;
import com.ztech.travelholic.Activities.HomeActivity;
import com.ztech.travelholic.Activities.UserAlbumActivity;
import com.ztech.travelholic.Adapter.MangeAlbumAdapter;
import com.ztech.travelholic.App;
import com.ztech.travelholic.Model.Album;
import com.ztech.travelholic.Model.User;
import com.ztech.travelholic.R;
import com.ztech.travelholic.Utils.CommonFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AlbumFragment extends Fragment implements View.OnClickListener, MaterialSearchBar.OnSearchActionListener {

    FloatingActionsMenu albumOptions;
    FloatingActionButton addAlbum, myAlbums;
    ShimmerFrameLayout shimmer;
    static DatabaseReference rootReference;
    static ArrayList<Album> list;
    static ArrayList<User> userList;
    static ArrayList<Album> suggestionList;
    static MangeAlbumAdapter adapter;
    static RecyclerView recyclerView;
    int checker = 0;
    static ArrayList<String> idList;
    static MaterialSearchBar searchBar;
    CustomSuggestionsAdapter customSuggestionsAdapter;
    static Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        init(view);
        setListener();
        getUserAllAlbum();
        return view;
    }


    private void setListener() {
        addAlbum.setOnClickListener(this::onClick);
        myAlbums.setOnClickListener(this::onClick);
        albumOptions.setOnClickListener(this::onClick);
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                searchBar.closeSearch();
                Log.d("LOG_TAG", text + "");
                User userAlbum = null;
                for (User user : userList) {
                    if (user.getUsername().equalsIgnoreCase(text.toString())) {
                        userAlbum = user;
                    }
                }
                if (userAlbum == null) {
                    CommonFunctions.showShortToastError(getContext(), "No such Albumist exist");
                } else {
                    getUserAlbum(userAlbum);
                }
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                switch (buttonCode) {
                    case MaterialSearchBar.BUTTON_SPEECH:
                        break;
                    case MaterialSearchBar.BUTTON_BACK:
                        searchBar.closeSearch();
                        break;
                }
            }
        });
    }

    private void init(View view) {
        activity = (Activity)view.getContext();
        albumOptions = view.findViewById(R.id.albumOptions);
        addAlbum = view.findViewById(R.id.addAlbum);
        myAlbums = view.findViewById(R.id.myAlbums);
        shimmer = view.findViewById(R.id.shimmer_view_container_home);
        shimmer.startShimmer();
        rootReference = FirebaseDatabase.getInstance().getReference();
        list = new ArrayList<>();
        userList = new ArrayList<>();
        idList = new ArrayList<>();
        suggestionList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.AlbumList);

        searchBar = view.findViewById(R.id.searchBar);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        customSuggestionsAdapter = new CustomSuggestionsAdapter(inflater);

        searchBar.setMaxSuggestionCount(2);
        searchBar.setHint("Find Albumist..");

        customSuggestionsAdapter.setSuggestions(suggestionList);
        searchBar.setCustomSuggestionAdapter(customSuggestionsAdapter);

        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                customSuggestionsAdapter.getFilter().filter(searchBar.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.albumOptions:
                break;
            case R.id.addAlbum:
                SendUserToActivity(new AddAlbumActivity());
                albumOptions.collapse();
                break;
            case R.id.myAlbums:
                SendUserToActivity(new UserAlbumActivity());
                albumOptions.collapse();
                break;
        }
    }

    private void SendUserToActivity(Activity activity) {
        Intent intent = new Intent(getContext(), activity.getClass());
        getContext().startActivity(intent);
    }

    private void getUserAllAlbum() {
        list.clear();
        idList.clear();
        suggestionList.clear();
        rootReference.child("Albums").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String key = snapshot.getKey();
                    if (key != null) {
                        Album album = snapshot.getValue(Album.class);
                        list.add(album);

                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        Collections.reverse(list);
                        adapter = new MangeAlbumAdapter(getContext(), list, getActivity(), 0);
                        recyclerView.setAdapter(adapter);
                        if (!idList.contains(album.getUser().getID())) {
                            userList.add(album.getUser());
                            suggestionList.add(album);
                            idList.add(album.getUser().getID());
                            customSuggestionsAdapter.setSuggestions(suggestionList);
                        }
                    }
                }
                shimmer.stopShimmer();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    static private void getUserAlbum(User user) {
        list.clear();
        rootReference.child("UserAlbums").child(user.getID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String key = snapshot.getKey();
                    if (key != null) {
                        Album album = snapshot.getValue(Album.class);
                        list.add(album);

                        LinearLayoutManager layoutManager = new LinearLayoutManager(App.getAppContext(), LinearLayoutManager.VERTICAL, false);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        adapter = new MangeAlbumAdapter(App.getAppContext(), list, activity, 0);
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checker == 1) {
            getUserAllAlbum();
        } else {
            checker = 1;
        }
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {

    }

    @Override
    public void onSearchConfirmed(CharSequence text) {

    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_SPEECH:
                break;
            case MaterialSearchBar.BUTTON_BACK:
                searchBar.closeSearch();
                break;
        }
    }

    static public void getCustomClicker(String text) {
        try {
            searchBar.setText(text);
            searchBar.closeSearch();
            User userAlbum = null;
            for (User user : userList) {
                if (user.getUsername().equalsIgnoreCase(text.toString())) {
                    userAlbum = user;
                }
            }
            if (userAlbum == null) {
                CommonFunctions.showShortToastError(searchBar.getContext(), "No such Albumist exist");
            } else {
                getUserAlbum(userAlbum);
            }
        } catch (Exception e) {

        }
    }

}