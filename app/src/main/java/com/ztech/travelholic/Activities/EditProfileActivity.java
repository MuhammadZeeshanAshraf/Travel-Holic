package com.ztech.travelholic.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.ztech.travelholic.R;
import com.ztech.travelholic.Utils.CommonFunctions;
import com.ztech.travelholic.databinding.ActivityEditProfileBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class EditProfileActivity extends AppCompatActivity {
    ActivityEditProfileBinding binding;
    DatabaseReference rootRef;
    Uri profileUri;
    StorageReference profileImageReference;
    String profileUriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        rootRef = FirebaseDatabase.getInstance().getReference();
        profileImageReference = FirebaseStorage.getInstance().getReference().child("UsersImages");
        profileUriString = "";
        setContentView(binding.getRoot());
        Wave wave = new Wave();
        binding.LoadingBar.setIndeterminateDrawable(wave);
        binding.LoadingBar.setVisibility(View.INVISIBLE);
        if (HomeActivity.HOME_USER != null) {
            binding.Name.setText(HomeActivity.HOME_USER.getUsername());
            binding.Email.setText(HomeActivity.HOME_USER.getEmail());
            binding.Phone.setText(HomeActivity.HOME_USER.getPhoneNumber());
            binding.Address.setText(HomeActivity.HOME_USER.getAddress());
            binding.Profession.setText(HomeActivity.HOME_USER.getProfession());
            Picasso.get().load(HomeActivity.HOME_USER.getUri()).placeholder(R.drawable.person_picture).error(R.drawable.person_picture).into(binding.PreviewImage);
        }

        binding.editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckField();
            }
        });
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(EditProfileActivity.this);
            }
        });
    }

    private void CheckField() {
        try {
            final String name = binding.Name.getText().toString();
            final String email = binding.Email.getText().toString();
            final String phone = binding.Phone.getText().toString();
            final String address = binding.Address.getText().toString();
            final String profession = binding.Profession.getText().toString();

            if (TextUtils.isEmpty(name)) {
                CommonFunctions.setError(binding.Name, "Username Required");
            } else if (TextUtils.isEmpty(email)) {
                CommonFunctions.setError(binding.Email, "Email Required");

            } else if (TextUtils.isEmpty(phone)) {
                CommonFunctions.setError(binding.Phone, "Phone Number Required");
            } else if (TextUtils.isEmpty(address)) {
                CommonFunctions.setError(binding.Address, "Full Address Required");
            } else if (TextUtils.isEmpty(profession)) {
                CommonFunctions.setError(binding.Profession, "Profession Required");
            } else {
                if (CommonFunctions.isNetworkAvailable(this)) {
                    EditUserProfile(name, email, phone, address, profession);
                } else {
                    CommonFunctions.showShortToastInfo(this, "Check Your Internet ! Make Sure Your are Connected to Internet ");
                }


            }
        } catch (Exception e) {
            Toast.makeText(EditProfileActivity.this, "Try Again, Something wrong occur while registration", Toast.LENGTH_SHORT).show();
        }
    }

    private void EditUserProfile(String name, String email, String phone, String address, String profession) {
        try {
            binding.LoadingBar.setVisibility(View.VISIBLE);

            Map MessageMap = new HashMap<>();
            MessageMap.put("ID", HomeActivity.HOME_USER.getID());
            MessageMap.put("Email", email);
            MessageMap.put("Address", address);
            MessageMap.put("Password", HomeActivity.HOME_USER.getPassword());
            MessageMap.put("Username", name);
            MessageMap.put("PhoneNumber", phone);
            MessageMap.put("Profession", profession);


            if (profileUri != null) {
                if (profileUri.equals(Uri.EMPTY)) {
                    MessageMap.put("Uri", HomeActivity.HOME_USER.getUri());
                    rootRef.child("Users").child(HomeActivity.HOME_USER.getID()).setValue(MessageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                HomeActivity.HOME_USER.setAddress(address);
                                HomeActivity.HOME_USER.setUsername(name);
                                HomeActivity.HOME_USER.setPhoneNumber(phone);
                                HomeActivity.HOME_USER.setProfession(profession);
                                startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                binding.LoadingBar.setVisibility(View.GONE);
                                CommonFunctions.showShortToastWarning(EditProfileActivity.this, "Some Problem happen will editing user...!");

                            }
                        }
                    });

                } else {
                    try {
                        binding.LoadingBar.setVisibility(View.VISIBLE);
                        File actualImage = new File(profileUri.getPath());
                        Bitmap compressedImage = new Compressor(EditProfileActivity.this)
                                .setMaxWidth(300)
                                .setMaxHeight(300)
                                .setQuality(100)
                                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                                .compressToBitmap(actualImage);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] final_image = baos.toByteArray();

                        final StorageReference filePath = profileImageReference.child(profileUri.getLastPathSegment() + ".jpg");

                        UploadTask uploadTask = filePath.putBytes(final_image);

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        MessageMap.put("Uri", uri + "");

                                        rootRef.child("Users").child(HomeActivity.HOME_USER.getID()).setValue(MessageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    HomeActivity.HOME_USER.setAddress(address);
                                                    HomeActivity.HOME_USER.setUsername(name);
                                                    HomeActivity.HOME_USER.setPhoneNumber(phone);
                                                    HomeActivity.HOME_USER.setProfession(profession);
                                                    startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                                                    finish();
                                                } else {
                                                    binding.LoadingBar.setVisibility(View.GONE);
                                                    CommonFunctions.showShortToastWarning(EditProfileActivity.this, "Some Problem happen will editing user...!");

                                                }
                                            }
                                        });
                                    }
                                });

                            }
                        });
                    } catch (Exception error) {
                        binding.LoadingBar.setVisibility(View.GONE);
                        Toasty.error(EditProfileActivity.this, "Some Problem happen will editing Users...!" +error.getMessage(), Toasty.LENGTH_SHORT).show();
                    }
                }
            } else {
                MessageMap.put("Uri", HomeActivity.HOME_USER.getUri());
                rootRef.child("Users").child(HomeActivity.HOME_USER.getID()).setValue(MessageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            HomeActivity.HOME_USER.setAddress(address);
                            HomeActivity.HOME_USER.setUsername(name);
                            HomeActivity.HOME_USER.setPhoneNumber(phone);
                            HomeActivity.HOME_USER.setProfession(profession);
                            startActivity(new Intent(EditProfileActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            binding.LoadingBar.setVisibility(View.GONE);
                            CommonFunctions.showShortToastWarning(EditProfileActivity.this, "Some Problem happen will editing user...!");

                        }
                    }
                });

            }



        } catch (Exception error) {
            binding.LoadingBar.setVisibility(View.GONE);
            CommonFunctions.showShortToastWarning(EditProfileActivity.this, "Some Problem happen will editing user...!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                profileUri = result.getUri();
                profileUriString = profileUri.toString();


                File actualImage = new File(profileUri.getPath());

                Bitmap compressedImage = null;
                try {
                    compressedImage = new Compressor(this)
                            .setMaxWidth(250)
                            .setMaxHeight(250)
                            .setQuality(50)
                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                            .compressToBitmap(actualImage);
                    binding.PreviewImage.setImageBitmap(Bitmap.createScaledBitmap(compressedImage, 256, 256, true));
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toasty.error(EditProfileActivity.this, "" + error, Toasty.LENGTH_SHORT).show();

            }
        }
    }

}