package com.acfapp.acf;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIRetrofitClient {
    public static Retrofit retrofit = null;

    public static Retrofit getRetrofit(String URL) {

        retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //.client(client)


        return retrofit;
    }
}
