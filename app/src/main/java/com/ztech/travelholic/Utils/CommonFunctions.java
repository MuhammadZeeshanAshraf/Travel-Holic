package com.ztech.travelholic.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import es.dmoral.toasty.Toasty;

public class CommonFunctions {

    public static void showShortToast(Context context, String string) {
        Toasty.success(context , string , Toasty.LENGTH_SHORT).show();
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public static void setError(EditText editText, String errorMessage) {
        editText.setError(errorMessage);
    }

    public static void setError(TextInputEditText editText, String errorMessage) {
        editText.setError(errorMessage);
    }

    public static void setError(TextView textView, String errorMessage) {
        textView.setError(errorMessage);
    }


    public static void showShortToastError(Context context, String string) {
        Toasty.error(context , string , Toasty.LENGTH_SHORT).show();
    }



    public static void showShortToastWarning(Context context, String string) {
        Toasty.warning(context , string , Toasty.LENGTH_SHORT).show();
    }


    public static void showShortToastInfo(Context context, String string) {
        Toasty.info(context , string , Toasty.LENGTH_SHORT).show();
    }



    public static void hideStatusBar(Activity context) {
        if (Build.VERSION.SDK_INT < 16) {
            context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = context.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public static void SendUserToActivity(Context context , Activity activity)
    {
        Intent intent = new Intent(context , activity.getClass());
        context.startActivity(intent);
    }




}