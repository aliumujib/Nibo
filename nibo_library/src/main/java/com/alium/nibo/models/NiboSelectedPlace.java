package com.alium.nibo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by abdulmujibaliu on 9/7/17.
 */

public class NiboSelectedPlace implements Parcelable {
    private double latitude;
    private double longitude;
    private String placeId;
    private String placeAddress;


    public NiboSelectedPlace(double latitude, double longitude, String placeId, String placeAddress) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeId = placeId;
        this.placeAddress = placeAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPlaceId() {
        return placeId;
    }

    protected NiboSelectedPlace(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        placeId = in.readString();
        placeAddress = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(placeId);
        dest.writeString(placeAddress);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NiboSelectedPlace> CREATOR = new Creator<NiboSelectedPlace>() {
        @Override
        public NiboSelectedPlace createFromParcel(Parcel in) {
            return new NiboSelectedPlace(in);
        }

        @Override
        public NiboSelectedPlace[] newArray(int size) {
            return new NiboSelectedPlace[size];
        }
    };

    public String getPlaceAddress() {
        return placeAddress;
    }


}
