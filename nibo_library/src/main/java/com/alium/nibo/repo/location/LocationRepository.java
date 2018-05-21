package com.alium.nibo.repo.location;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.alium.nibo.repo.contracts.ILocationRepository;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by abdulmujibaliu on 9/3/17.
 */

public class LocationRepository implements ILocationRepository {

    private GoogleApiClient mGoogleApiClient;
    private Context context;

    public LocationRepository(GoogleApiClient mGoogleApiClient, Context context) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.context = context;
        this.mGoogleApiClient.connect();
    }

    @Override
    public Observable<Location> getLocationObservable() {
        return Observable.create(new ObservableOnSubscribe<Location>() {
            @Override
            public void subscribe(final ObservableEmitter<Location> source) throws Exception {
                FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(context);
                locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            source.onNext(location);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        source.onError(e);
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
