package com.example.myapplication2;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

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
    Call<String> uploadFile(@Field("files") String fileDir);
}
