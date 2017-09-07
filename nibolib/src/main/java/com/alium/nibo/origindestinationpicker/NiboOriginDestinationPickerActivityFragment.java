package com.alium.nibo.origindestinationpicker;

import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alium.nibo.R;
import com.alium.nibo.base.BaseNiboFragment;
import com.alium.nibo.repo.location.LocationRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * A placeholder fragment containing a simple view.
 */
public class NiboOriginDestinationPickerActivityFragment extends BaseNiboFragment {

    public NiboOriginDestinationPickerActivityFragment() {
    }

    @Override
    protected void extractGeocode(double lati, double longi) {

    }

    @Override
    protected void handleMarkerAddition(LatLng latLng) {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initmap();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_origin_destination_picker, container, false);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        super.onMarkerDragStart(marker);
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
