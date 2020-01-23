package com.acfapp.acf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class APIRetrofitClient {
    public static Retrofit retrofit = null;
    public static Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    public static Retrofit getRetrofit(String URL) {

        retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();



        return retrofit;
    }


    public static APIInterface getRetrofitClient(String URL) {

        retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        APIInterface api = retrofit.create(APIInterface.class);


        return api;
    }
}
