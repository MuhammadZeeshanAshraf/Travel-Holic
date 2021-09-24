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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.ztech.travelholic.R;
import com.ztech.travelholic.Utils.CommonFunctions;
import com.ztech.travelholic.databinding.ActivityRegisterBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    FirebaseAuth firebaseAuth;
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
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
    }

    private void init() {
        profileUri = null;
        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        profileImageReference = FirebaseStorage.getInstance().getReference().child("UsersImages");
        Wave wave = new Wave();
        binding.LoadingBar.setIndeterminateDrawable(wave);
        binding.LoadingBar.setVisibility(View.INVISIBLE);
        profileUriString = "";
    }

    private void setListener() {
        binding.UserSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckField();
            }
        });

        binding.UserSignUpGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(RegisterActivity.this);
            }
        });

    }

    private void CheckField() {
        try {
            final String name = binding.UserSignUpName.getText().toString();
            final String email = binding.UserSignUpEmail.getText().toString();
            final String phone = binding.UserSignUpPhone.getText().toString();
            final String address = binding.UserSignUpAddress.getText().toString();
            final String profession = binding.UserSignUpProfession.getText().toString();
            final String password = binding.UserSignUpPassword.getText().toString();
            final String confirm = binding.UserSignUpConfirmPassword.getText().toString();

            if (TextUtils.isEmpty(name)) {
                CommonFunctions.setError(binding.UserSignUpName, "Username Required");
            }

            if (TextUtils.isEmpty(email)) {
                CommonFunctions.setError(binding.UserSignUpEmail, "Email Required");
            } else {
                if (!isEmailValid(email)) {
                    CommonFunctions.setError(binding.UserSignUpEmail, "Email is Invalid");
                    Toasty.error(RegisterActivity.this, "Email is Invalid", Toasty.LENGTH_SHORT).show();
                }
            }
            if (TextUtils.isEmpty(phone)) {
                CommonFunctions.setError(binding.UserSignUpPhone, "Phone Number Required");
            }
            if (TextUtils.isEmpty(address)) {
                CommonFunctions.setError(binding.UserSignUpAddress, "Full Address Required");
            }
            if (TextUtils.isEmpty(profession)) {
                CommonFunctions.setError(binding.UserSignUpProfession, "Profession Required");
            }
            if (TextUtils.isEmpty(password)) {
                CommonFunctions.setError(binding.UserSignUpPassword, "Password Required");
            }
            if (TextUtils.isEmpty(confirm)) {
                CommonFunctions.setError(binding.UserSignUpConfirmPassword, "Confirm Password Required");
            }
            if (!(password.equals(confirm))) {
                CommonFunctions.setError(binding.UserSignUpConfirmPassword, "Password and Confirm Password must be same");
            }

            if (TextUtils.isEmpty(name)) {
                CommonFunctions.setError(binding.UserSignUpName, "Username Required");
            } else if (TextUtils.isEmpty(email)) {
                CommonFunctions.setError(binding.UserSignUpEmail, "Email Required");

            } else if (TextUtils.isEmpty(phone)) {
                CommonFunctions.setError(binding.UserSignUpPhone, "Phone Number Required");
            } else if (TextUtils.isEmpty(address)) {
                CommonFunctions.setError(binding.UserSignUpAddress, "Full Address Required");
            } else if (TextUtils.isEmpty(profession)) {
                CommonFunctions.setError(binding.UserSignUpProfession, "Profession Required");
            } else if (TextUtils.isEmpty(password)) {
                CommonFunctions.setError(binding.UserSignUpPassword, "Password Required");
            } else if (TextUtils.isEmpty(confirm)) {
                CommonFunctions.setError(binding.UserSignUpConfirmPassword, "Confirm Password Required");
            } else if (!(password.equals(confirm))) {
                CommonFunctions.setError(binding.UserSignUpConfirmPassword, "Password and Confirm Password must be same");
            } else {
                if (CommonFunctions.isNetworkAvailable(this)) {
                    if(profileUri != null)
                    {
                        if (profileUri.equals(Uri.EMPTY)) {
                            Toasty.error(RegisterActivity.this, "Select Preview Image of the User", Toasty.LENGTH_SHORT).show();

                        } else {
                            RegisterUserWithFirebase(name, email, phone, password, address, profession);
                        }
                    }else
                    {
                        Toasty.error(RegisterActivity.this, "Select Preview Image of the User", Toasty.LENGTH_SHORT).show();

                    }

                } else {
                    CommonFunctions.showShortToastInfo(this, "Check Your Internet ! Make Sure Your are Connected to Internet ");
                }


            }
        } catch (Exception e) {
            Toast.makeText(RegisterActivity.this, "Try Again, Something wrong occur while registration " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void RegisterUserWithFirebase(String name, String email, String phone, String password, String address, String profession) {
        try {
            binding.LoadingBar.setVisibility(View.VISIBLE);
            File actualImage = new File(profileUri.getPath());
            firebaseAuth.signOut();
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        try {
                            Bitmap compressedImage = new Compressor(RegisterActivity.this)
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

                                            FirebaseUser user = firebaseAuth.getCurrentUser();
                                            assert user != null;
                                            user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    CommonFunctions.showShortToastInfo(RegisterActivity.this , "Verification email has been sent. Please Verify your email!");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    CommonFunctions.showShortToastError(RegisterActivity.this , "Error occur while sending verification email." + e.getMessage());
                                                }
                                            });

                                            final String currentUserID = firebaseAuth.getCurrentUser().getUid();
                                            Map MessageMap = new HashMap<>();
                                            MessageMap.put("ID", currentUserID);
                                            MessageMap.put("Email", email);
                                            MessageMap.put("Address", address);
                                            MessageMap.put("Password", password);
                                            MessageMap.put("Username", name);
                                            MessageMap.put("PhoneNumber", phone);
                                            MessageMap.put("Profession", profession);
                                            MessageMap.put("Uri", uri + "");

                                            rootRef.child("Users").child(currentUserID).setValue(MessageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                                        finish();
                                                    } else {
                                                        binding.LoadingBar.setVisibility(View.GONE);
                                                        CommonFunctions.showShortToastWarning(RegisterActivity.this, "Some Problem happen will adding user...!");

                                                    }
                                                }
                                            });
                                        }
                                    });

                                }
                            });
                        } catch (Exception error) {
                            binding.LoadingBar.setVisibility(View.GONE);
                            Toasty.error(RegisterActivity.this, "Some Problem happen will adding Users...!", Toasty.LENGTH_SHORT).show();
                        }


                    } else {
                        binding.LoadingBar.setVisibility(View.GONE);
                        CommonFunctions.showShortToastWarning(RegisterActivity.this, "Some Problem happen will adding user...!");

                    }
                }
            });


        } catch (Exception error) {
            binding.LoadingBar.setVisibility(View.GONE);
            CommonFunctions.showShortToastWarning(RegisterActivity.this, "Some Problem happen will adding user...!");
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
                Toasty.error(RegisterActivity.this, "" + error, Toasty.LENGTH_SHORT).show();

            }
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}