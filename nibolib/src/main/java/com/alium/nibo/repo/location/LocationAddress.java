package com.alium.nibo.repo.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;

public class LocationAddress {

    private static final String TAG = "LocationAddress";

    public static final LocationAddress sharedLocationAddressInstance = new LocationAddress();

    Address address;

    public Observable<Address> getObservableAddressFromLocation(final double latitude, final double longitude,
                                                                final Context context) {
        return new Observable<Address>() {
            @Override
            protected void subscribeActual(Observer<? super Address> observer) {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        address = addressList.get(0);
                        observer.onNext(address);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable connect to Geocoder", e);
                }

            }
        };
    }

    public Address getAddressFromLocation(final double latitude, final double longitude,
                                          final Context context) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> addressList = geocoder.getFromLocation(
                    latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                address = addressList.get(0);

            }
        } catch (IOException e) {
            Log.e(TAG, "Unable connect to Geocoder", e);
        }
        return address;


    }


}

