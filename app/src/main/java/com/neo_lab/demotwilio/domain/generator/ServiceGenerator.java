package com.neo_lab.demotwilio.domain.generator;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sam_nguyen on 11/04/2017.
 */

public class ServiceGenerator {

    private static final String API_BASE_URL = "https://calling-dev.appspot.com";

    private static final String DATE_FORMAT = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'";

    private static Retrofit retrofit;

    private static Gson mGson = new GsonBuilder()
            .setDateFormat(DATE_FORMAT)
            .create();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(mGson));

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    public static <S> S createService(Class<S> serviceClass) {

        if (!httpClient.interceptors().contains(logging)) {
            httpClient.addInterceptor(logging);
            builder.client(httpClient.build());
            retrofit = builder.build();
        }

        retrofit = builder.client(httpClient.build()).build();

        return retrofit.create(serviceClass);
    }

    /**
     * or Error Handing when non-OK response is received from Server
     */
    @NonNull
    public static Retrofit retrofit() {
        OkHttpClient client =  httpClient.build();;
        return builder.client(client).build();
    }



}
