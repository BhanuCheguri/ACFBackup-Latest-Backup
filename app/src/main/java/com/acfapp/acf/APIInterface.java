package com.acfapp.acf;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIInterface {

    String BASE_URL = "http://api.ainext.in/";
    //String GET_POSTS =  http://api.ainext.in/posts/getposts?categoryID=1&days=-1
    //String GET_MEMBERS =  BASE_URL + "members/getmembers?mobile=9032200318/";

    @GET("members/getmembers?")
    Call<List<MyProfileModel>> getProfileDetails(@Query("mobile") String mobile);

    @GET("posts/getposts?categoryID=1&days=-1")
    Call<List<WallPostsModel>> getWallPostDetails();
}
