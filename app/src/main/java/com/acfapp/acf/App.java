package com.acfapp.acf;


import android.app.Application;

import com.acfapp.acf.SMSVerification.AppSignatureHelper;

/**
 * Created on : May 21, 201Secure@1239
 * Author     : AndroidWave
 */
public class App extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
    appSignatureHelper.getAppSignatures();
  }
}
