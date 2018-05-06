package com.alium.nibo.di;

import com.alium.nibo.utils.NiboConstants;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by aliumujib on 03/05/2018.
 */

public class RetrofitModule {

    private Retrofit retrofit;
    private static RetrofitModule retrofitModule;

    public static RetrofitModule getInstance() {
        if (retrofitModule == null) {
            retrofitModule = new RetrofitModule();
        }
        return retrofitModule;
    }

    private RetrofitModule() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);


            retrofit = new Retrofit.Builder()
                    .baseUrl(NiboConstants.BASE_DIRECTIONS_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
    }

    public Retrofit providesRetrofit() {
        return retrofit;
    }

}
