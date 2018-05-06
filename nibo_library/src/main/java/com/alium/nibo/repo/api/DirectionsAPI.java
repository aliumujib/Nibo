package com.alium.nibo.repo.api;


import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by mohak on 2/8/17.
 */

public interface DirectionsAPI {

    @GET("/maps/api/directions/json")
    Call<JsonObject> getPolylineData(@Query("origin") String origin, @Query("destination") String destination, @Query("key") String apiKey);

}
