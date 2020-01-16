package com.acfapp.acf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    SharedPreferences pref;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, this.getClass().toString());
        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
    }


    public void setActionBarTitle(String strTitle)
    {
        getSupportActionBar().setTitle(strTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
    }

    public boolean getBooleanSharedPreference(Activity activity,String key){
        return pref.getBoolean(key, false);
    }

    public void putBooleanSharedPreference(Activity activity,String key,boolean value)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key,value); // Storing boolean - true/false
        editor.commit(); // commit changes
    }

    public String getStringSharedPreference(Activity activity,String key){
        return pref.getString(key, "");
    }

    public void putStringSharedPreference(Activity activity,String key,String value)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key,value); // Storing boolean - true/false
        editor.commit(); // commit changes
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showErrorAlert(Activity activity, String strMsg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // Get the layout inflater
        LayoutInflater inflater = (activity).getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the
        // dialog layout
        builder.setTitle("Alert");
        builder.setMessage(strMsg);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.alert);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    public void showProgressDialog(Context context){
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading...Please wait");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public void hideProgressDialog(Context context)
    {
        if(mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}