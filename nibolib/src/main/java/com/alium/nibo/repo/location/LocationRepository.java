package com.alium.nibo.repo.location;

import android.app.Activity;
import android.location.Location;

import com.alium.nibo.repo.contracts.ILocationRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import io.reactivex.subjects.PublishSubject;

/**
 * Created by abdulmujibaliu on 9/3/17.
 */

public class LocationRepository implements ILocationRepository {

    Location userLocation;

    PublishSubject<Location> publishSubject = PublishSubject.create();

    private FusedLocationProviderClient mFusedLocationClient;

    public LocationRepository(Activity activity) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
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

    @Override
    public PublishSubject<Location> getLocationObservable() {
        return publishSubject;
    }
}
