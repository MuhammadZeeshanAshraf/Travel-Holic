package com.ztech.travelholic.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.ztech.travelholic.Activities.EditProfileActivity;
import com.ztech.travelholic.Activities.HomeActivity;
import com.ztech.travelholic.Activities.LoginActivity;
import com.ztech.travelholic.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment implements View.OnClickListener {

    TextView username, email, address, profession, phone,verfyText;
    LinearLayout logout;
    RelativeLayout gotoEdit;
    CircleImageView profileImage;
    ImageView vefy;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    DatabaseReference rootReference;
    String currentUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);



        username = view.findViewById(R.id.username);
        email = view.findViewById(R.id.email);
        address = view.findViewById(R.id.address);
        profession = view.findViewById(R.id.profession);
        logout = view.findViewById(R.id.logout);
        phone = view.findViewById(R.id.phone);
        gotoEdit = view.findViewById(R.id.gotoEdit);
        profileImage = view.findViewById(R.id.edit_profile_image);
        vefy = view.findViewById(R.id.verf);
        verfyText = view.findViewById(R.id.verfyText);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        firebaseAuth.signInWithEmailAndPassword(HomeActivity.HOME_USER.getEmail(), HomeActivity.HOME_USER.getPassword()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                currentUser = firebaseAuth.getCurrentUser();
                rootReference = FirebaseDatabase.getInstance().getReference();
                currentUserId = currentUser.getUid();
                settingUserProfileValue();
                if(currentUser.isEmailVerified())
                {
                    vefy.setImageResource(R.drawable.badge);
                    verfyText.setText("Verified");
                    verfyText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            }
        });
        gotoEdit.setOnClickListener(this::onClick);
        logout.setOnClickListener(this::onClick);


        return view;
    }

    private void settingUserProfileValue() {
        if (HomeActivity.HOME_USER != null) {
            username.setText(HomeActivity.HOME_USER.getUsername());
            email.setText(HomeActivity.HOME_USER.getEmail());
            phone.setText(HomeActivity.HOME_USER.getPhoneNumber());
            address.setText(HomeActivity.HOME_USER.getAddress());
            profession.setText(HomeActivity.HOME_USER.getProfession());
            if (!HomeActivity.HOME_USER.getUri().isEmpty()) {
                Picasso.get().load(HomeActivity.HOME_USER.getUri()).placeholder(R.drawable.person_picture).error(R.drawable.person_picture).into(profileImage);
            }
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.logout:
                firebaseAuth.signOut();
                SendUserToActivity(new LoginActivity());
                getActivity().finish();
                break;
            case R.id.gotoEdit:
                SendUserToActivity(new EditProfileActivity());
                break;

        }
    }

    private void SendUserToActivity(Activity activity) {
        Intent intent = new Intent(getContext(), activity.getClass());
        getContext().startActivity(intent);
    }
}