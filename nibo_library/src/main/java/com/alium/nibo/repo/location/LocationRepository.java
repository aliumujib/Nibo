package com.alium.nibo.repo.location;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.alium.nibo.repo.contracts.ILocationRepository;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

/**
 * Created by abdulmujibaliu on 9/3/17.
 */

public class LocationRepository implements ILocationRepository {

    private final ReactiveLocationProvider mLocationProvider;
    Location userLocation;

    PublishSubject<Location> publishSubject = PublishSubject.create();

    private GoogleApiClient mGoogleApiClient;

    public LocationRepository(Activity activity, GoogleApiClient mGoogleApiClient) {
        mLocationProvider = new ReactiveLocationProvider(activity);
        this.mGoogleApiClient = mGoogleApiClient;
        this.mGoogleApiClient.connect();
    }

    public Observable<Place> getPlaceByID(final String placeId) {
        return new Observable<Place>() {
            @Override
            protected void subscribeActual(final Observer<? super Place> observer) {
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(@NonNull PlaceBuffer places) {
                                if (places.getStatus().isSuccess()) {
                                    final Place thatPlace = places.get(0);
                                    LatLng queriedLocation = thatPlace.getLatLng();
                                    Log.v("Latitude is", "" + queriedLocation.latitude);
                                    Log.v("Longitude is", "" + queriedLocation.longitude);

                                    observer.onNext(thatPlace.freeze());
                                }
                                places.release();
                            }
                        });
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Location> getLocationObservable() {
        return mLocationProvider.getLastKnownLocation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
