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

    ActivityOtpverificationBinding binding;
    GoogleApiClient mGoogleApiClient;
    SmsBroadcastReceiver mSmsBroadcastReceiver;
    private int RESOLVE_HINT = 2;
    private RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setActionBarTitle(getString(R.string.title_OTPVerification));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_otpverification);
        binding.btnContinue.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.changeMobileNo.setOnClickListener(this);

        mSmsBroadcastReceiver = new SmsBroadcastReceiver();

        //set google api client for hint request
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        mSmsBroadcastReceiver.setOnOtpListeners(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        getApplicationContext().registerReceiver(mSmsBroadcastReceiver, intentFilter);

        // get mobile number from phone
        //getHintPhoneNumber();

        //requestQueue = Volley.newRequestQueue(this);
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
                // Call server API for requesting OTP and when you got success start SMS Listener for listing auto read message lsitner
                //startSMSListener();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                if(binding.etMobileNo.getText().toString().isEmpty()){
                    Snack("Number is empty");
                }else if(binding.etMobileNo.getText().length()!=10){
                    Snack("Check the number");
                }else if(binding.etMobileNo.getText().toString().isEmpty()){
                    Snack("Message is empty");
                }else{
                    //SendSms(binding.etMobileNo.getText().toString(),"OTP");
                    Toast.makeText(this, "SMS gateway is in Development", Toast.LENGTH_SHORT).show();
                    binding.otpCode.setText("91001");
                }


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
                    Intent intent = new Intent(OTPVerificationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
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
