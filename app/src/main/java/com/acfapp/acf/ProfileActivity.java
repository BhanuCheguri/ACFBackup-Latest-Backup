package com.acfapp.acf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.acfapp.acf.databinding.ActivityProfileBinding;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ProfileActivity extends BaseActivity {
    APIRetrofitClient apiRetrofitClient;
    ActivityProfileBinding binding;
    String strLoginType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_profile);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        setActionBarTitle("My Profile");
        init();
    }

    private void getProfileDetails() {
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<List<MyProfileModel>> call = api.getProfileDetails("9032200318");

        call.enqueue(new Callback<List<MyProfileModel>>() {
            @Override
            public void onResponse(Call<List<MyProfileModel>> call, Response<List<MyProfileModel>> response) {
                List<MyProfileModel> myProfileData = response.body();

                String[] heroes = new String[myProfileData.size()];

                //looping through all the heroes and inserting the names inside the string array
                for (int i = 0; i < myProfileData.size(); i++) {
                    //heroes[i] = myProfileData.get(i).getFullName();
                    binding.email.setText(myProfileData.get(i).getEmail());
                    binding.name.setText(myProfileData.get(i).getFullName());
                    binding.mobileNo.setText(myProfileData.get(i).getMobile());
                    if(myProfileData.get(i).getGender().equalsIgnoreCase("M"))
                        binding.gender.setText("Male");
                    else if(myProfileData.get(i).getGender().equalsIgnoreCase("F"))
                        binding.gender.setText("Female");

                }
            }

            @Override
            public void onFailure(Call<List<MyProfileModel>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void init()
    {
        apiRetrofitClient = new APIRetrofitClient();
        getProfileDetails();
        strLoginType = getStringSharedPreference(ProfileActivity.this, "LoginType");

        binding.myPostings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this,ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    public void signOut()
    {
       /* if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }*/
        try {
            if (strLoginType.equalsIgnoreCase("Google")) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(ProfileActivity.this, gso);
                googleSignInClient.signOut();
                putBooleanSharedPreference(ProfileActivity.this, "LoggedIn",false);
                Toast.makeText(getApplicationContext(), "User Logged out successfully", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(ProfileActivity.this, NewLoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                FacebookSdk.sdkInitialize(getApplicationContext());
                AppEventsLogger.activateApp(ProfileActivity.this);
                LoginManager.getInstance().logOut();
                putBooleanSharedPreference(ProfileActivity.this, "LoggedIn",false);

                Intent intent = new Intent(ProfileActivity.this, NewLoginActivity.class);
                startActivity(intent);
                finish();
            }
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }
    }
}
