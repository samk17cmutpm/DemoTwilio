package com.neo_lab.demotwilio.domain.services;

import com.neo_lab.demotwilio.domain.response.TokenServer;


import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by sam_nguyen on 11/04/2017.
 */

public interface TokenService {

    @GET("token")
    Observable<Response<TokenServer>> getTokenChatting(@Query("device") String deviceId, @Query("username") String userName);

}