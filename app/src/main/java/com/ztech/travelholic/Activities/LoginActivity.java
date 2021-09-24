package com.ztech.travelholic.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.ImageRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ztech.travelholic.Model.User;
import com.ztech.travelholic.R;
import com.ztech.travelholic.Utils.CommonFunctions;
import com.ztech.travelholic.databinding.ActivityLoginBinding;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference rootRef;
    DatabaseReference userRef;
    GoogleSignInClient mGoogleSignInClient;
    final int RC_SIGN_IN = 10;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListener();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        callbackManager = CallbackManager.Factory.create();
        printHashKey(LoginActivity.this);
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Wave wave = new Wave();
        binding.LoadingBar.setIndeterminateDrawable(wave);
        binding.LoadingBar.setVisibility(View.INVISIBLE);
    }

    private void setListener() {
        binding.forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = binding.UserLoginEmail.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    CommonFunctions.setError(binding.UserLoginEmail, "Email Required");
                    Toast.makeText(LoginActivity.this, "Email Required for forget password request", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toasty.info(LoginActivity.this , "Reset Password Email sent",Toasty.LENGTH_SHORT).show();
                                    } else {
                                        CommonFunctions.showShortToastError(LoginActivity.this, "Email account does not exist on the provide email...");

                                    }
                                }
                            });
                }

            }
        });
        binding.UserSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckField();
            }
        });

        binding.UserLoginGoToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        binding.btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });
        binding.btnLoginFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();


                                    GraphRequest request = GraphRequest.newMeRequest(
                                            loginResult.getAccessToken(),
                                            new GraphRequest.GraphJSONObjectCallback() {
                                                @Override
                                                public void onCompleted(JSONObject object, GraphResponse response) {

                                                    Log.v("LoginActivity", response.toString());

                                                    try {
                                                        String obj = object.toString();
                                                        String name = object.getString("name");
                                                        String id = object.getString("id");
                                                        String profileURL = "";
                                                        if (Profile.getCurrentProfile() != null) {
                                                            profileURL = ImageRequest.getProfilePictureUri(Profile.getCurrentProfile().getId(), 400, 400).toString();
                                                        }

                                                        Log.v("completeObjInfo", obj);
                                                        Log.v("TAGObjectInfo", "id: " + id);
                                                        Log.v("TAGObjectInfo", "name: " + name);
                                                        Log.v("TAGObjectInfo", "profileURL: " + profileURL);
                                                        String finalProfileURL = profileURL;

                                                        Map MessageMap = new HashMap<>();
                                                        MessageMap.put("ID", user.getUid());
                                                        MessageMap.put("Email", "facebook Login");
                                                        MessageMap.put("Address", "");
                                                        MessageMap.put("Password", "");
                                                        MessageMap.put("Username", name);
                                                        MessageMap.put("PhoneNumber", "");
                                                        MessageMap.put("Profession", "");
                                                        MessageMap.put("Uri", profileURL + "");

                                                        rootRef.child("Users").child(user.getUid()).setValue(MessageMap);

                                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                                        finishAffinity();

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            }
                                    );
                                    Bundle parameters = new Bundle();
                                    parameters.putString("fields", "id,name,email,gender,birthday");                    //set these parameter
                                    request.setParameters(parameters);
                                    request.executeAsync();

                                } else {
                                    Toast.makeText(LoginActivity.this, "Login Failed With Facebook " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this, "User cancelled login from Facebook", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void CheckField() {
        try {
            final String email = binding.UserLoginEmail.getText().toString();
            final String password = binding.UserLoginPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                CommonFunctions.setError(binding.UserLoginEmail, "Email Required");

            }
            if (TextUtils.isEmpty(password)) {
                CommonFunctions.setError(binding.UserLoginPassword, "Password Required");
            }

            if (TextUtils.isEmpty(email)) {
                CommonFunctions.setError(binding.UserLoginEmail, "Email Required");

            } else if (TextUtils.isEmpty(password)) {
                CommonFunctions.setError(binding.UserLoginPassword, "Password Required");
            } else {
                if (CommonFunctions.isNetworkAvailable(this)) {
                    AllowUserToLogin(email, password);
                } else {
                    CommonFunctions.showShortToastInfo(this, "Check Your Internet ! Make Sure Your are Connected to Internet ");
                }


            }
        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "Try Again, Something wrong occur while Logging in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void AllowUserToLogin(final String email, final String password) {

        binding.LoadingBar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    final String currentUserId = firebaseAuth.getCurrentUser().getUid();
                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {

                            if (task.isSuccessful()) {
                                String devicetoken = task.getResult().getToken();
                                userRef.child(currentUserId).child("DeviceToken").setValue(devicetoken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            binding.LoadingBar.setVisibility(View.INVISIBLE);
                                            Intent home_intent = new Intent(LoginActivity.this, HomeActivity.class);
                                            home_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(home_intent);
                                            finish();
                                            Toasty.success(LoginActivity.this, "Login Successfully", Toasty.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(LoginActivity.this, "Try Again, Something wrong occur while Logging in.", Toast.LENGTH_SHORT).show();
                                binding.LoadingBar.setVisibility(View.INVISIBLE);
                            }

                        }
                    });

                } else {
                    String error_message = task.getException().toString();
                    Toast.makeText(LoginActivity.this, "Try Again, Something wrong occur while Logging in.", Toast.LENGTH_SHORT).show();
                    binding.LoadingBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);   //For Facebook Login
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken(), account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }


    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
                            String personName = acct.getDisplayName();
                            String personEmail = acct.getEmail();
                            String personId = acct.getId();
                            Uri personPhoto = acct.getPhotoUrl();

                            FirebaseUser currentFirebaseUser =FirebaseAuth.getInstance().getCurrentUser();
                            String userId=currentFirebaseUser.getUid();
                            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference userNameRef = rootRef.child("Users").child(user.getUid());
                            userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists())
                                    {
                                        Map MessageMap = new HashMap<>();
                                        MessageMap.put("ID", user.getUid());
                                        MessageMap.put("Email", personEmail);
                                        MessageMap.put("Address", "");
                                        MessageMap.put("Password", "");
                                        MessageMap.put("Username", personName);
                                        MessageMap.put("PhoneNumber", "");
                                        MessageMap.put("Profession", "");
                                        MessageMap.put("Uri", personPhoto + "");

                                        rootRef.child("Users").child(user.getUid()).setValue(MessageMap);

                                    }
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finishAffinity();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });




                        } else {
                            Log.w("TAG", "signInWithCredential:failure", task.getException());

                        }
                    }
                });
    }

    public static void printHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("zee", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e("zee", "printHashKey()", e);
        } catch (Exception e) {
            Log.e("zee", "printHashKey()", e);
        }
    }
}