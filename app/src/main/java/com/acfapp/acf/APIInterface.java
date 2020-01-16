package com.acfapp.acf;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIInterface {

    String BASE_URL = "http://api.ainext.in/";
    //String GET_POSTS =  http://api.ainext.in/posts/getposts?categoryID=1&days=-1
    //String GET_MEMBERS =  BASE_URL + "members/getmembers?mobile=9032200318/";
    //String ADD_MEMBERS =  BASE_URL + "members/addmember";

    @GET("members/getmembers?")
    Call<List<MyProfileModel>> getProfileDetails(@Query("mobile") String mobile);

    @GET("posts/getposts?categoryID=1&days=-1")
    Call<List<WallPostsModel>> getWallPostDetails();

    //@Headers("Content-Type: application/json")
   /* @FormUrlEncoded
    @POST("members/addmember")
    *//*Call<AddMemberResult> postAddMember(@Field("fullname") String fullname,
                                        @Field("mobile") String mobile,
                                        @Field("email") String email,
                                        @Field("gender") String gender,
                                        @Field("photo") String photo);*//*
    Call<AddMemberResult> postAddMember(@Body AddMemberData addData_pojo);*/

    @PATCH("members/addmember")
    Call<AddMemberResult> postAddMember(@Body AddMemberData addData_pojo);

}
