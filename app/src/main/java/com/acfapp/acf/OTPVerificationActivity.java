package com.acfapp.acf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.acfapp.acf.SMSVerification.OtpReceivedInterface;
import com.acfapp.acf.SMSVerification.SmsBroadcastReceiver;
import com.acfapp.acf.databinding.ActivityOtpverificationBinding;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OTPVerificationActivity extends BaseActivity implements View.OnClickListener , GoogleApiClient.ConnectionCallbacks,
        OtpReceivedInterface, GoogleApiClient.OnConnectionFailedListener{

    String TAG = "OTPVerificationActivity.class" ;
    ActivityOtpverificationBinding binding;
    GoogleApiClient mGoogleApiClient;
    SmsBroadcastReceiver mSmsBroadcastReceiver;
    private int RESOLVE_HINT = 2;
    APIRetrofitClient apiRetrofitClient;
    private RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setActionBarTitle(getString(R.string.title_OTPVerification));

        apiRetrofitClient = new APIRetrofitClient();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_otpverification);
        binding.btnContinue.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.changeMobileNo.setOnClickListener(this);
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

    public void startSMSListener() {
        SmsRetrieverClient mClient = SmsRetriever.getClient(this);
        Task<Void> mTask = mClient.startSmsRetriever();
        mTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {
                String strMobileNo = binding.etMobileNo.getText().toString();
                if (!strMobileNo.equalsIgnoreCase("") && strMobileNo != null)
                {
                    binding.verifyOtp.setText(getString(R.string.verify_otp) + "\t" + strMobileNo);
                    binding.llVerifyingOtp.setVisibility(View.VISIBLE);
                    binding.llVerifyMobileNo.setVisibility(View.GONE);
                    Toast.makeText(OTPVerificationActivity.this, "SMS Retriever starts", Toast.LENGTH_LONG).show();
                }
                else
                    showErrorAlert(OTPVerificationActivity.this,getString(R.string.alert_verify_mobile_no));
                //Toast.makeText(OTPVerificationActivity.this,"Please enter Mobile Number to verify",Toast.LENGTH_LONG).show();

            }
        });
        mTask.addOnFailureListener(new OnFailureListener() {
            @Override public void onFailure(@NonNull Exception e) {
                Toast.makeText(OTPVerificationActivity.this, "Error", Toast.LENGTH_LONG).show();
            }
        });
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
                postAddMemberDetails();

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

                    postAddMemberDetails();

                    /*Intent intent = new Intent(OTPVerificationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();*/
                }
        }
    }

    private void postAddMemberDetails() {

        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);

        showProgressDialog(OTPVerificationActivity.this);
        Call<AddMemberResult> call;



        try {

           AddMemberData addData_pojo = new AddMemberData();
            addData_pojo.setEmail("RAM");
            addData_pojo.setFullname("4384984990");
            addData_pojo.setMobile("sample@gmail.com");
            addData_pojo.setGender("M");
            addData_pojo.setPhoto("ewerw.jpg");
           /*  JSONObject paramObject = new JSONObject();
            paramObject.put("fullname", "RAM");
            paramObject.put("mobile", "4384984990");
            paramObject.put("email", "sample@gmail.com");
            paramObject.put("gender", "M");
            paramObject.put("photo", "ewerw.jpg");*/
            String body = "{\n" +
                    "\"fullname\":\"Rama K Eddy\",\n" +
                    "\"mobile\": \"7678765897\",\n" +
                    "\"email\":\"ghfhf@gmail.com\",\n" +
                    "\"gender\":\"M\",\n" +
                    "\"photo\":\"rama.jpg\"\n" +
                    "}";
            call = api.postAddMember(body);
            //call = api.postAddMember("RAM","4384984990","sample@gmail.com","M","ewerw.jpg");

            call.enqueue(new Callback<AddMemberResult>() {

                @Override
                public void onResponse(Call<AddMemberResult> call, retrofit2.Response<AddMemberResult> response) {
                    if (response != null && response.isSuccessful()) {
                        Log.i(TAG, "post submitted to API." + response.body().toString());

                        String strMemberID = response.body().getMemberID();
                        String strFullName = response.body().getFullName();
                        String strMobile = response.body().getMobile();
                        String strEmail = response.body().getEmail();
                        String strGender = response.body().getGender();
                        String strPhoto = response.body().getPhoto();
                        String strMemberType = response.body().getMemberType();
                        String strRegDate = response.body().getRegDate();
                        String strStatus = response.body().getStatus();
                        String strModifiedBy = response.body().getModifiedBy();
                        String strModifiedDate = response.body().getModifiedDate();
                    } else {
                        //response.body() have your LoginResult fields and methods  (example you have to access error then try like this response.body().getError() )
                        Toast.makeText(getBaseContext(), response.body().toString(), Toast.LENGTH_SHORT).show();
                        hideProgressDialog(OTPVerificationActivity.this);
                    }
                }

                @Override
                public void onFailure(Call<AddMemberResult> call, Throwable t) {
                    Log.e(TAG, "onFailure: message" + t.getMessage());
                    Log.e(TAG, "Unable to submit post to API." + t.getMessage());

                    t.printStackTrace();
                    hideProgressDialog(OTPVerificationActivity.this);
                    Toast.makeText(OTPVerificationActivity.this, "Unable to submit post to API.", Toast.LENGTH_SHORT).show();
                }

            });
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    public void Snack(String message){
        Toast.makeText(OTPVerificationActivity.this, message, Toast.LENGTH_LONG).show();
    }

    public void SendSms(final String to, final String message) {

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
    }
}
