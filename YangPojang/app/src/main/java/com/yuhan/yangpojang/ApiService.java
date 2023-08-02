package com.yuhan.yangpojang;

import com.yuhan.yangpojang.VerificationRequest;
import com.yuhan.yangpojang.VerificationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("your-endpoint-url") // Replace with your actual endpoint URL
    Call<VerificationResponse> verifyUser(@Body VerificationRequest request);
}
