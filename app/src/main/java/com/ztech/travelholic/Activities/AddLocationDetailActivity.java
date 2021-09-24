package com.ztech.travelholic.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.ztech.travelholic.R;
import com.ztech.travelholic.databinding.ActivityAddLocationDetailBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class AddLocationDetailActivity extends AppCompatActivity {

    ActivityAddLocationDetailBinding binding;
    DatabaseReference rootRef;
    Uri productUri;
    StorageReference ProductImageReference;
    ArrayList<String> categoryList;
    String category = "Valley", productUriString;
    String lan, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = ActivityAddLocationDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListener();


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(AddLocationDetailActivity.this,
                R.array.category, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.catSpinner.setAdapter(adapter);
        binding.catSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object Location) {
                category = Location.toString();
            }
        });

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void init() {
        rootRef = FirebaseDatabase.getInstance().getReference();
        Wave wave = new Wave();
        binding.LoadingBar.setIndeterminateDrawable(wave);
        binding.LoadingBar.setVisibility(View.INVISIBLE);

        Intent i = getIntent();
        lan = i.getStringExtra("lan");
        lon = i.getStringExtra("lon");

        productUri = null;
        ProductImageReference = FirebaseStorage.getInstance().getReference().child("LocationImages");


        category = "Valley";
        productUriString = "";
    }

    private void setListener() {

        binding.addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckFields();
            }
        });

        binding.itemCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(AddLocationDetailActivity.this);
            }
        });

    }

    private void CheckFields() {
        try {
            final String title = binding.title.getText().toString();
            final String desc = binding.description.getText().toString();

            if (TextUtils.isEmpty(title)) {
                binding.title.setError("Title Required");
                Toasty.error(AddLocationDetailActivity.this, "Enter Album Title", Toasty.LENGTH_SHORT).show();

            } else if (category.equals("")) {
                Toasty.error(AddLocationDetailActivity.this, "Select Album Category", Toasty.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(desc)) {
                binding.description.setError("Description Required");
                Toasty.error(AddLocationDetailActivity.this, "Enter Album Description", Toasty.LENGTH_SHORT).show();

            } else {
                if (!(TextUtils.isEmpty(desc))) {
                    if (!(TextUtils.isEmpty(title))) {
                        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                        ConnectivityManager cm =
                                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        boolean isConnected = activeNetwork != null &&
                                activeNetwork.isConnectedOrConnecting();


                        if (mWifi.isConnected() || isConnected) {


                            if (productUri.equals(Uri.EMPTY)) {
                                Toasty.error(AddLocationDetailActivity.this, "Select Preview Image of the Location", Toasty.LENGTH_SHORT).show();

                            } else {
                                AddItemToDatabase(title, desc);
                            }

                        } else {
                            Toasty.warning(AddLocationDetailActivity.this, "Check Your Internet ! Make Sure Your are Connected to Internet ", Toasty.LENGTH_SHORT).show();
                        }


                    }

                }
            }

        } catch (Exception e) {
            binding.LoadingBar.setVisibility(View.GONE);
            if (productUriString.equals("")) {
                Toasty.error(AddLocationDetailActivity.this, "Select Preview Image of the Location", Toasty.LENGTH_SHORT).show();

            } else {
                Toasty.error(AddLocationDetailActivity.this, "Some Problem happen will adding the Location...!", Toasty.LENGTH_SHORT).show();

            }

        }
    }

    private void AddItemToDatabase(String title, String desc) {
        binding.LoadingBar.setVisibility(View.VISIBLE);
        File actualImage = new File(productUri.getPath());
        try {
            Bitmap compressedImage = new Compressor(this)
                    .setMaxWidth(300)
                    .setMaxHeight(300)
                    .setQuality(100)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .compressToBitmap(actualImage);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] final_image = baos.toByteArray();

            final StorageReference filePath = ProductImageReference.child(productUri.getLastPathSegment() + ".jpg");

            UploadTask uploadTask = filePath.putBytes(final_image);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            DatabaseReference userKeyRef = rootRef.child("Locations").push();

                            final String PushID = userKeyRef.getKey();

                            Map MessageMap = new HashMap<>();
                            MessageMap.put("ID", PushID);
                            MessageMap.put("Title", title);
                            MessageMap.put("Description", desc);
                            MessageMap.put("Category", category);
                            MessageMap.put("Uri", uri + "");
                            MessageMap.put("Lan", lan);
                            MessageMap.put("Lon", lon);
                            MessageMap.put("Rate", 3);

                            rootRef.child("Locations").child(PushID).updateChildren(MessageMap);
                            rootRef.child("UserLocations").child(HomeActivity.HOME_USER.getID()).child(PushID).updateChildren(MessageMap);
                            rootRef.child("CategoryLocations").child(category).child(PushID).updateChildren(MessageMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    binding.LoadingBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(AddLocationDetailActivity.this , HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    });

                }
            });
        } catch (Exception error) {
            binding.LoadingBar.setVisibility(View.GONE);
            Toasty.error(AddLocationDetailActivity.this, "Some Problem happen will adding Location...!", Toasty.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                productUri = result.getUri();
                productUriString = productUri.toString();


                File actualImage = new File(productUri.getPath());

                Bitmap compressedImage = null;
                try {
                    compressedImage = new Compressor(this)
                            .setMaxWidth(250)
                            .setMaxHeight(250)
                            .setQuality(50)
                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                            .compressToBitmap(actualImage);
                    binding.itemPreviewImage.setImageBitmap(Bitmap.createScaledBitmap(compressedImage, 256, 256, true));
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toasty.error(AddLocationDetailActivity.this, "" + error, Toasty.LENGTH_SHORT).show();

            }
        }
    }

}