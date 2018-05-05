package com.alium.nibo.repo.api;


import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by mohak on 2/8/17.
 */

public interface GeocodeAPI {

    @GET("/maps/api/geocode/json")
    Call<JsonObject> getPolylineData(@Query("latlng") String latLng, @Query("key") String apiKey);
}
