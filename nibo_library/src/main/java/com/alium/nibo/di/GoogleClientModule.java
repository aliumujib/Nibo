package com.alium.nibo.di;

import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class GoogleClientModule {


    private AppCompatActivity activity;
    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks;
    private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener;

    protected GoogleApiClient googleApiClient;


    public GoogleClientModule(AppCompatActivity activity,
                              GoogleApiClient.ConnectionCallbacks connectionCallbacks,
                              GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
        this.activity = activity;
        this.connectionCallbacks = connectionCallbacks;
        this.connectionFailedListener = connectionFailedListener;
    }



    public GoogleApiClient getGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient
                    .Builder(activity)
                    .enableAutoManage(activity, 0, connectionFailedListener)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addConnectionCallbacks(connectionCallbacks)
                    .addOnConnectionFailedListener(connectionFailedListener)
                    .build();
        }

        return googleApiClient;
    }


}
