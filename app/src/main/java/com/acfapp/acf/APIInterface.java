package com.acfapp.acf;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
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

   // @Headers("Content-Type: application/json")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("members/addmember")
    Call<AddMemberResult> postAddMember(@Body JsonObject jsonBody);

    @POST("posts")
    Call<AddMemberResult> postData(@Body JsonObject body);

    @Multipart
    @POST("posts/upload")
    Call<JsonObject> uploadImage(@Part MultipartBody.Part image);
}
