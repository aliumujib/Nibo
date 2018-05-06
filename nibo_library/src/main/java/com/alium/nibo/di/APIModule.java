package com.alium.nibo.di;

import com.alium.nibo.repo.api.DirectionsAPI;
import com.alium.nibo.repo.api.GeocodeAPI;

/**
 * Created by aliumujib on 03/05/2018.
 */

public class APIModule {

    private static APIModule apiModule;

    private RetrofitModule retrofitModule;
    private DirectionsAPI directionsAPI;
    private GeocodeAPI geocodeAPI;

    public static APIModule getInstance(RetrofitModule retrofitModule) {
        if (apiModule == null) {
            apiModule = new APIModule(retrofitModule);
        }
        return apiModule;
    }


    public DirectionsAPI getDirectionsAPI() {
        if (directionsAPI == null) {
            directionsAPI = retrofitModule.providesRetrofit().create(DirectionsAPI.class);
        }
        return directionsAPI;
    }


    public GeocodeAPI getGeocodeAPI() {
        if (geocodeAPI == null) {
            geocodeAPI = retrofitModule.providesRetrofit().create(GeocodeAPI.class);
        }
        return geocodeAPI;
    }

    public APIModule(RetrofitModule retrofitModule) {
        this.retrofitModule = retrofitModule;
    }

}
