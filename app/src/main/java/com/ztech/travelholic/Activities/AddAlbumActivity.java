package com.ztech.travelholic.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.coursion.freakycoder.mediapicker.galleries.Gallery;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.ztech.travelholic.Adapter.AddAlbumAdapter;
import com.ztech.travelholic.R;
import com.ztech.travelholic.databinding.ActivityAddAlbumBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class AddAlbumActivity extends AppCompatActivity {
    ActivityAddAlbumBinding binding;
    String Category = "Valley";
    DatabaseReference rootRef;
    Uri AlbumUri;
    StorageReference AlbumImageReference;
    String AlbumUriString;
    AlertDialog waitingDialog;
    private final int OPEN_MEDIA_PICKER = 1;
    AddAlbumAdapter addAlbumAdapter;
    ArrayList<String> selectionResult = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = ActivityAddAlbumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        init();
        setListener();
        checkPermissionOfApp();
    }

    private void checkPermissionOfApp() {
        if (ContextCompat.checkSelfPermission(AddAlbumActivity.this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(AddAlbumActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(AddAlbumActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(AddAlbumActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {

                    } else {
                        requestStoragePermission();
                    }
                } else {
                    requestStoragePermission();
                }
            } else {
                requestStoragePermission();
            }
        } else {
            requestStoragePermission();
        }

    }

    private void init() {
        AlbumUri = null;
        rootRef = FirebaseDatabase.getInstance().getReference();
        AlbumImageReference = FirebaseStorage.getInstance().getReference().child("AlbumsImages");
        Wave wave = new Wave();
        binding.LoadingBar.setIndeterminateDrawable(wave);
        binding.LoadingBar.setVisibility(View.INVISIBLE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(AddAlbumActivity.this,
                R.array.category, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.CatSpinner.setAdapter(adapter);
        AlbumUriString = "";
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
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.multiImageSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionOfApp();
                if (ContextCompat.checkSelfPermission(AddAlbumActivity.this, Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddAlbumActivity.this, new String[]{Manifest.permission.CAMERA},
                            50);
                } else {
                    Toasty.info(AddAlbumActivity.this, "Select 3 Album Pictures", Toasty.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddAlbumActivity.this, Gallery.class);
                    intent.putExtra("title", "Select media");
                    intent.putExtra("mode", 2);
                    intent.putExtra("maxSelection", 8);
                    startActivityForResult(intent, OPEN_MEDIA_PICKER);
                }


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<ImageView> imageViewList = new ArrayList<>();

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                AlbumUri = result.getUri();
                AlbumUriString = AlbumUri.toString();


                File actualImage = new File(AlbumUri.getPath());

                Bitmap compressedImage = null;
                try {
                    compressedImage = new Compressor(this)
                            .setMaxWidth(250)
                            .setMaxHeight(250)
                            .setQuality(50)
                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                            .compressToBitmap(actualImage);
                    binding.multiImageSelector.setImageBitmap(Bitmap.createScaledBitmap(compressedImage, 256, 256, true));
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toasty.error(AddAlbumActivity.this, "" + error, Toasty.LENGTH_SHORT).show();

            }
        }

        if (requestCode == OPEN_MEDIA_PICKER) {
            if (resultCode == RESULT_OK && data != null) {
                selectionResult = data.getStringArrayListExtra("result");
                FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(AddAlbumActivity.this);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setFlexWrap(FlexWrap.WRAP);
                layoutManager.setAlignItems(AlignItems.STRETCH);
                binding.imageList.setLayoutManager(layoutManager);
                binding.imageList.setItemAnimator(new DefaultItemAnimator());
                addAlbumAdapter = new AddAlbumAdapter(AddAlbumActivity.this, selectionResult, AddAlbumActivity.this, 0);
                binding.imageList.setAdapter(addAlbumAdapter);
                binding.multiImageSelector.setVisibility(View.GONE);
            }
        }
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddAlbumActivity.this);
        //setting up the layout for alert dialog
        View view = LayoutInflater.from(AddAlbumActivity.this).inflate(R.layout.dialog_please_wait, null, false);
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

            if (selectionResult.size() < 1) {
                Toasty.error(AddAlbumActivity.this, "Select 3 Album Pictures", Toasty.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(title)) {
                binding.title.setError("Title Required");
            }
            if (Category.equals("")) {
                Toasty.error(AddAlbumActivity.this, "Select Album Category", Toasty.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(desc)) {
                binding.description.setError("Description Required");
            }

            if (selectionResult.size() < 1) {
                Toasty.error(AddAlbumActivity.this, "Select 3 Album Pictures", Toasty.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(title)) {
                binding.title.setError("Title Required");
                Toasty.error(AddAlbumActivity.this, "Enter Album Title", Toasty.LENGTH_SHORT).show();

            } else if (Category.equals("")) {
                Toasty.error(AddAlbumActivity.this, "Select Album Category", Toasty.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(desc)) {
                binding.description.setError("Description Required");
                Toasty.error(AddAlbumActivity.this, "Enter Album Description", Toasty.LENGTH_SHORT).show();

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
                            Toasty.warning(AddAlbumActivity.this, "Check Your Internet ! Make Sure Your are Connected to Internet ", Toasty.LENGTH_SHORT).show();
                        }
                    }

                }
            }

        } catch (Exception e) {
            binding.LoadingBar.setVisibility(View.GONE);
            if (AlbumUriString.equals("")) {
                Toasty.error(AddAlbumActivity.this, "Select Preview Image of the album item", Toasty.LENGTH_SHORT).show();

            } else {
                Toasty.error(AddAlbumActivity.this, "Some Problem happen will adding the item...!", Toasty.LENGTH_SHORT).show();

            }

        }
    }

    private void AddItemToDatabase(String name, String desc) {
        showLoadingDialog();
        binding.LoadingBar.setVisibility(View.VISIBLE);
        ArrayList<String> uriList = new ArrayList<>();
        try {
            DatabaseReference userKeyRef = rootRef.child("Albums").push();
            final String PushID = userKeyRef.getKey();
            for (int i = 0; i < selectionResult.size(); i++) {
                File actualImage = new File(selectionResult.get(i));
//                Bitmap compressedImage = new Compressor(this)
//                        .setMaxWidth(300)
//                        .setMaxHeight(300)
//                        .setQuality(100)
//                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
//                        .compressToBitmap(actualImage);
                Bitmap compressedImage = new Compressor(this)
                        .setQuality(80)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .compressToBitmap(actualImage);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] final_image = baos.toByteArray();
                Uri uriFromPath = Uri.fromFile(actualImage);
                final StorageReference filePath = AlbumImageReference.child(uriFromPath.getLastPathSegment() + ".jpg");

                UploadTask uploadTask = filePath.putBytes(final_image);

                int finalI = i;
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uriList.add(uri + "");
                                if (finalI == (selectionResult.size() - 1)) {
                                    Map MessageMap = new HashMap<>();
                                    MessageMap.put("ID", PushID);
                                    MessageMap.put("Title", name);
                                    MessageMap.put("Description", desc);
                                    MessageMap.put("Category", Category);
                                    MessageMap.put("Uri", uriList);
                                    MessageMap.put("User", HomeActivity.HOME_USER);

                                    rootRef.child("Albums").child(PushID).updateChildren(MessageMap);
                                    rootRef.child("UserAlbums").child(HomeActivity.HOME_USER.getID()).child(PushID).updateChildren(MessageMap);
                                    rootRef.child("CategoryAlbums").child(Category).child(PushID).updateChildren(MessageMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            waitingDialog.dismiss();
                                            finish();
                                        }
                                    });
                                }

                            }
                        });

                    }
                });
            }


        } catch (Exception error) {
            error.printStackTrace();
            waitingDialog.dismiss();
            binding.LoadingBar.setVisibility(View.GONE);
            Toasty.error(AddAlbumActivity.this, "Some Problem happen will adding Album...!", Toasty.LENGTH_SHORT).show();
        }

    }


    private void requestStoragePermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddAlbumActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
}