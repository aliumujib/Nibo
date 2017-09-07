package com.alium.nibo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by abdulmujibaliu on 9/7/17.
 */

public class NiboSelectedPlace implements Parcelable {
    LatLng latLng;
    String placeId;
    String placeAddress;


    public NiboSelectedPlace(LatLng latLng, String placeId, String placeAddress) {
        this.latLng = latLng;
        this.placeId = placeId;
        this.placeAddress = placeAddress;
    }

    protected NiboSelectedPlace(Parcel in) {
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        placeId = in.readString();
        placeAddress = in.readString();
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

    public String getPlaceId() {
        return placeId;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(latLng, flags);
        dest.writeString(placeId);
        dest.writeString(placeAddress);
    }
}
