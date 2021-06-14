package com.example.myapplication2;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by SYSTEM on 07/06/2021.
 */
public interface ApiInterface {

    @FormUrlEncoded
    @POST("auth/login")
    Call<AuthModel> authentication(@Field("user") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("auth/login")
    Call<AuthModel> authProof(@Field("username") String username, @Field("password") String password);

    @POST("patient")
    Call<String> uploadFileOld(@Field("files") String fileDir);

    @Multipart
    @POST("patient")
    Call<ResponseBody> uploadFile( @Part MultipartBody.Part file, @Header("Authorization") String token);
}
