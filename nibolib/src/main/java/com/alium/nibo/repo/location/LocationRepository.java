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
import io.reactivex.subjects.PublishSubject;

/**
 * Created by abdulmujibaliu on 9/3/17.
 */

public class LocationRepository implements ILocationRepository {

    Location userLocation;

    PublishSubject<Location> publishSubject = PublishSubject.create();

    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;

    public LocationRepository(Activity activity, GoogleApiClient mGoogleApiClient) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        this.mGoogleApiClient = mGoogleApiClient;
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLocation = location;
                            publishSubject.onNext(userLocation);

                            //addOverlay(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
                            //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        } else {
                            userLocation = null;
                        }
                    }
                });
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

                                    observer.onNext(thatPlace);
                                }
                                places.release();
                            }
                        });
            }
        };
    }

    @Override
    public PublishSubject<Location> getLocationObservable() {
        return publishSubject;
    }
}
