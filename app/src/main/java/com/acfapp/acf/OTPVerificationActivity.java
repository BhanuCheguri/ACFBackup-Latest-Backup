package com.acfapp.acf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.acfapp.acf.Receiver.MySMSBroadcastReceiver;
import com.acfapp.acf.SMSVerification.OtpReceivedInterface;
import com.acfapp.acf.SMSVerification.SmsBroadcastReceiver;
import com.acfapp.acf.databinding.ActivityOtpverificationBinding;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class OTPVerificationActivity extends BaseActivity implements View.OnClickListener , GoogleApiClient.ConnectionCallbacks,
        OtpReceivedInterface, GoogleApiClient.OnConnectionFailedListener{

    String TAG = "OTPVerificationActivity.class" ;
    ActivityOtpverificationBinding binding;
    GoogleApiClient mGoogleApiClient;
    SmsBroadcastReceiver mSmsBroadcastReceiver;
    private int RESOLVE_HINT = 2;
    APIRetrofitClient apiRetrofitClient;
    private RequestQueue requestQueue;
    JsonObject gsonObject;
    private static final int CREDENTIAL_PICKER_REQUEST = 1;  // Set to an unused request code
    private int MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setActionBarTitle(getString(R.string.title_OTPVerification));

        gsonObject = new JsonObject();

        apiRetrofitClient = new APIRetrofitClient();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otpverification);
        binding.btnContinue.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.changeMobileNo.setOnClickListener(this);


        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECEIVE_SMS},
                MY_PERMISSIONS_REQUEST_SMS_RECEIVE);
    }

    @Override public void onConnected(@Nullable Bundle bundle) {

    }

    @Override public void onConnectionSuspended(int i) {

    }

    @Override public void onOtpReceived(String otp) {
        Toast.makeText(this, "Otp Received " + otp, Toast.LENGTH_LONG).show();
        binding.etMobileNo.setText(otp);
    }

    @Override public void onOtpTimeout() {
        Toast.makeText(this, "Time out, please resend", Toast.LENGTH_LONG).show();
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public void getHintPhoneNumber() {
        HintRequest hintRequest =
                new HintRequest.Builder()
                        .setPhoneNumberIdentifierSupported(true)
                        .build();
        PendingIntent mIntent = Auth.CredentialsApi.getHintPickerIntent(mGoogleApiClient, hintRequest);
        try {
            startIntentSenderForResult(mIntent.getIntentSender(), RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SMS_RECEIVE) {
            Log.d("TAG", "My permission request sms received successfully");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Result if we want hint number
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == Activity.RESULT_OK) {

                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId();  <-- will need to process phone number string
                binding.etMobileNo.setText(credential.getId());
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.btnContinue:
                //postAddMemberDetails("Rama K Eddy", "7678765897", "ghfhf@gmail.com", "M", "rama.jpg");
                //postImageUpload();
                getOTP();
                break;
            case R.id.change_mobile_no:
                binding.llVerifyingOtp.setVisibility(View.GONE);
                binding.llVerifyMobileNo.setVisibility(View.VISIBLE);
                break;
            case R.id.btnSubmit:
                if(binding.etMobileNo.getText().toString().equalsIgnoreCase("") &&
                        binding.etMobileNo.getText().toString() == null )
                {
                    Toast.makeText(this,"Mobile Number cannot be empty",Toast.LENGTH_LONG).show();
                }else {

                    postAddMemberDetails("Rama K Eddy","7678765897","ghfhf@gmail.com","M","rama.jpg");
                    //postImageUpload();
                    /*Intent intent = new Intent(OTPVerificationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();*/
                }
        }
    }

    public static boolean isValidMobile(String phone) {
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            if (phone.length() < 9 ) {
                // if(phone.length() != 10) {
                check = false;
                // txtPhone.setError("Not Valid Number");
            } else {
                check = android.util.Patterns.PHONE.matcher(phone).matches();
            }
        } else {
            check = false;
        }
        return check;
    }




    private void getOTP() {
        try{
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        if(!binding.etMobileNo.getText().toString().equalsIgnoreCase("")) {
            if (!isValidMobile(binding.etMobileNo.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "Please enter the valid Mobile Number to get OTP", Toast.LENGTH_SHORT).show();
            } else {
                Call<ResponseBody> call = api.getSMSOTP(binding.etMobileNo.getText().toString());

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        ResponseBody myProfileData = response.body();
                        binding.llVerifyMobileNo.setVisibility(View.GONE);
                        binding.llVerifyingOtp.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Mobile Number shouldn't be empty", Toast.LENGTH_SHORT).show();
        }
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }
    }

    private void postAddMemberDetails(String fullname, String mobile, String email, String gender, String photo) {

        String body = "{\n" +
                "\"fullname\":\"Rama K Eddy\",\n" +
                "\"mobile\": \"7678765897\",\n" +
                "\"email\":\"ghfhf@gmail.com\",\n" +
                "\"gender\":\"M\",\n" +
                "\"photo\":\"rama.jpg\"\n" +
                "}";
        try {
            gsonObject.addProperty("fullname", "Rama K Eddy");
            gsonObject.addProperty("mobile", "7678765897");
            gsonObject.addProperty("email", "ghfhf@gmail.com");
            gsonObject.addProperty("gender", "M");
            gsonObject.addProperty("photo", "rama.jpg");

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);

        try {
            //showProgressDialog(OTPVerificationActivity.this);

            Call<AddMemberResult> registerCall = api.postData(gsonObject);

            registerCall.enqueue(new retrofit2.Callback<AddMemberResult>() {
                    @Override
                    public void onResponse(Call<AddMemberResult> registerCall, retrofit2.Response<AddMemberResult> response) {

                        try {
                            if(response.body() != null) {
                                Log.e(" responce => ", response.body().toString());
                                AddMemberResult addMemberResult = response.body();
                                if (response.isSuccessful()) {
                                   //hideProgressDialog(OTPVerificationActivity.this);
                                } else {
                                    //hideProgressDialog(OTPVerificationActivity.this);
                                }
                            }else
                                Toast.makeText(OTPVerificationActivity.this, "RESPONSE :: "+response.body(), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                Log.e("Tag", "error=" + e.toString());
                                Toast.makeText(OTPVerificationActivity.this, "ERROR :: "+ e.toString(), Toast.LENGTH_SHORT).show();
                                //hideProgressDialog(OTPVerificationActivity.this);
                            } catch (Resources.NotFoundException e1) {
                                e1.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<AddMemberResult> call, Throwable t) {
                        try {
                            Log.e("Tag", "error" + t.toString());
                            //Toast.makeText(OTPVerificationActivity.this, "ERROR :: "+ t.toString(), Toast.LENGTH_SHORT).show();
                            hideProgressDialog(OTPVerificationActivity.this);
                        } catch (Resources.NotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("otp"));
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("otp")) {
                final String message = intent.getStringExtra("number");
                Toast.makeText(OTPVerificationActivity.this, "OTP is :" + message, Toast.LENGTH_SHORT).show();
                // message is the fetching OTP
                if(message != null && !message.equalsIgnoreCase(""))
                    binding.otpCode.setText(message);
            }
        }
    };

    public void Snack(String message){
        Toast.makeText(OTPVerificationActivity.this, message, Toast.LENGTH_LONG).show();
    }


   /* public void SendSms(final String to, final String message) {

        StringRequest menuRequest = new StringRequest(Request.Method.POST,"http://example.com/androidsms/sms.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(response).getJSONArray("check");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        int i = jsonResponse.length();
                        for(int j=0;j<i;j++){
                            JSONObject jsonChildNode = null;
                            try {
                                jsonChildNode = jsonResponse.getJSONObject(j);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Snack(jsonChildNode.optString("sms").toString());

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Snack("Volley " + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("to",to);
                params.put("message",message);
                return params;
            }
        };
        requestQueue.add(menuRequest);
    }*/
}
