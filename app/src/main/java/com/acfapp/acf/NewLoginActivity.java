package com.acfapp.acf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import io.fabric.sdk.android.Fabric;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.acfapp.acf.databinding.ActivityNewLoginBinding;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class NewLoginActivity extends BaseActivity implements View.OnClickListener ,GoogleApiClient.OnConnectionFailedListener{

    ActivityNewLoginBinding binding;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private SignInButton mSignInButton;

    private GoogleApiClient mGoogleApiClient;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    //LoginButton loginButton;
    CallbackManager callbackManager;
    private FirebaseAnalytics mFirebaseAnalytics;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        try {
           // Crashlytics.getInstance().crash();

            boolean bLoggedIn = getBooleanSharedPreference(NewLoginActivity.this, "LoggedIn");
            if (bLoggedIn) {
                putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", false);
                Intent intent = new Intent(NewLoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                FacebookSdk.sdkInitialize(this.getApplicationContext());

                mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                binding = DataBindingUtil.setContentView(this, R.layout.activity_new_login);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
                }

                /***** G+ Login *****/

                mFirebaseAuth = FirebaseAuth.getInstance();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                        .requestEmail()
                        .build();
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                        .addApi(Plus.API)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();
                mGoogleApiClient.connect(); // <-- move to here.

                mAuth = FirebaseAuth.getInstance();
                mAuthListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // User is signed in
                            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                        } else {
                            // User is signed out
                            Log.d(TAG, "onAuthStateChanged:signed_out");
                        }
                    }
                };

                binding.signInButton.setOnClickListener(this);


                /***** Facebook Login *****/
                callbackManager = CallbackManager.Factory.create();


                //facebookLogin();
            }
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }
    }


    public class FacebookAyncTask extends AsyncTask<Void, Void, Void>{

        ProgressDialog progressDialog ;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(NewLoginActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Please wait...");
            progressDialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            facebookLogin();
            return null;
        }
    }


    public class GPlusAyncTask extends AsyncTask<Void, Void, Void>{

        ProgressDialog progressDialog ;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(NewLoginActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Please wait...");
            progressDialog.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            gplusLogin();
            return null;
        }
    }

    private void gplusLogin()
    {
        try {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private void facebookLogin()
    {
        try {
            boolean loggedOut = AccessToken.getCurrentAccessToken() == null;

            if (!loggedOut) {
                //Using Graph API
                getUserProfile(AccessToken.getCurrentAccessToken());
            }


            AccessTokenTracker fbTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                    if (accessToken2 == null) {
                        Toast.makeText(getApplicationContext(), "User Logged  out successfully.", Toast.LENGTH_LONG).show();
                    }
                }
            };
            fbTracker.startTracking();

            binding.loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));

            binding.loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
                    if (!loggedOut) {
                        Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName());
                        getUserProfile(AccessToken.getCurrentAccessToken());
                    }
                }

                @Override
                public void onCancel() {
                    System.out.println("onCancel");
                    Crashlytics.log("onCancel");
                }

                @Override
                public void onError(FacebookException exception) {
                    System.out.println("onError");
                    Crashlytics.logException(exception);
                }
            });

            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        }catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.login_button:
            case R.id.facebook:
                try {
                    if(isAppInstalled(getApplicationContext(), "com.facebook.katana")) {
                        new FacebookAyncTask().execute();
                    }else {
                        Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.facebook.katana"));
                        startActivity(i);
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
                break;
            case R.id.sign_in_button:
            case R.id.google:
                new GPlusAyncTask().execute();
                break;
        }
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        try {
            GraphRequest request = GraphRequest.newMeRequest(
                    currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            Log.d("TAG", object.toString());
                            try {
                                String personFirstName = object.getString("first_name");
                                String personLastEmail = object.getString("last_name");
                                String personName = object.getString("first_name") + " " + object.getString("last_name");
                                String personEmail = object.getString("email");
                                String personId = object.getString("id");
                                String image_url = "https://graph.facebook.com/" + personId + "/picture?type=large";
                                Toast.makeText(NewLoginActivity.this, personName + " " + personEmail, Toast.LENGTH_LONG).show();

                                putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", true);
                                putBooleanSharedPreference(NewLoginActivity.this, "LoggedIn", true);
                                putStringSharedPreference(NewLoginActivity.this, "LoginType", "Facebook");
                                putStringSharedPreference(NewLoginActivity.this, "PersonName", personName);
                                putStringSharedPreference(NewLoginActivity.this, "personEmail", personEmail);
                                putStringSharedPreference(NewLoginActivity.this, "personId", personId);


                                Toast.makeText(NewLoginActivity.this, "Successfully Registered", Toast.LENGTH_LONG);
                                Intent intent = new Intent(NewLoginActivity.this, OTPVerificationActivity.class);
                                intent.putExtra("PersonName", personName);
                                intent.putExtra("personEmail", personEmail);
                                intent.putExtra("personId", personId);

                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "first_name,last_name,email,id");
            request.setParameters(parameters);
            request.executeAsync();
        }catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
        Crashlytics.log(connectionResult.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                //handleSignInResult(result);

                if (result.isSuccess()) {

                    // Google Sign-In was successful, authenticate with Firebase
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                    if (account != null) {
                        String personName = account.getDisplayName();
                        String personGivenName = account.getGivenName();
                        String personFamilyName = account.getFamilyName();
                        String personEmail = account.getEmail();
                        String personId = account.getId();
                        //Uri personPhoto = account.getPhotoUrl();

                        putBooleanSharedPreference(NewLoginActivity.this, "FirstTime", true);
                        putBooleanSharedPreference(NewLoginActivity.this, "LoggedIn", true);
                        putStringSharedPreference(NewLoginActivity.this, "LoginType", "Google");
                        putStringSharedPreference(NewLoginActivity.this, "PersonName", personName);
                        putStringSharedPreference(NewLoginActivity.this, "personEmail", personEmail);
                        putStringSharedPreference(NewLoginActivity.this, "personId", personId);

                        Toast.makeText(NewLoginActivity.this, "Successfully Registered", Toast.LENGTH_LONG);
                        Intent intent = new Intent(NewLoginActivity.this, OTPVerificationActivity.class);
                        intent.putExtra("PersonName", personName);
                        intent.putExtra("personEmail", personEmail);
                        intent.putExtra("personId", personId);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                } else {
                    // Google Sign-In failed
                    Log.e(TAG, "Google Sign-In failed.");
                }
            }catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        try{
            if (result.isSuccess()) {

                final GoogleSignInAccount acct = result.getSignInAccount();

                String name = acct.getDisplayName();
                final String mail = acct.getEmail();
                // String photourl = acct.getPhotoUrl().toString();

                final String givenname="",familyname="",displayname="",birthday="";

                Plus.PeopleApi.load(mGoogleApiClient, acct.getId()).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {
                        Person person = loadPeopleResult.getPersonBuffer().get(0);

                        Log.d("GivenName ", person.getName().getGivenName());
                        Log.d("FamilyName ",person.getName().getFamilyName());
                        Log.d("DisplayName ",person.getDisplayName());
                        Log.d("gender ", String.valueOf(person.getGender())); //0 = male 1 = female
                        String gender="";
                        if(person.getGender() == 0){
                            gender = "Male";
                        }else {
                            gender = "Female";
                        }
                        Log.d("Gender ",gender);
                        if(person.hasBirthday()) {
                            Log.d("Birthday ", person.getBirthday());
                        }
                    }
                });
            } else {

                Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
            }
        }catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    /*private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(NewLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(NewLoginActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                });
    }*/

    public void showErrorAlert(Activity activity, String strMsg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
        builder1.setMessage(strMsg);
        builder1.setCancelable(true);
        builder1.setTitle("Alert");
        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
