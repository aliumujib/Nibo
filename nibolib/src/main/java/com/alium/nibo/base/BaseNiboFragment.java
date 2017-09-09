package com.alium.nibo.base;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;

import com.alium.nibo.R;
import com.alium.nibo.models.NiboSelectedPlace;
import com.alium.nibo.repo.location.LocationRepository;
import com.alium.nibo.repo.location.SuggestionsRepository;
import com.alium.nibo.utils.NiboStyle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by abdulmujibaliu on 9/7/17.
 */

public abstract class BaseNiboFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback, GoogleMap.OnMarkerDragListener {
    protected GoogleMap mMap;
    protected boolean hasWiderZoom;
    protected Marker mCurrentMapMarker;
    protected static final int DEFAULT_ZOOM = 16;
    protected static final int WIDER_ZOOM = 6;
    protected GoogleApiClient mGoogleApiClient;

    protected String mSearchBarTitle;
    protected String mConfirmButtonTitle;
    protected NiboStyle mStyleEnum = NiboStyle.DEFAULT;
    protected String TAG = getClass().getSimpleName();

    protected
    @RawRes
    int mStyleFileID;
    protected
    @DrawableRes
    int mMarkerPinIconRes;
    protected NiboSelectedPlace mCurrentSelection;
    protected LocationRepository mLocationRepository;
    protected FloatingActionButton mCenterMyLocationFab;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .enableAutoManage(getActivity(), 0, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        this.mCenterMyLocationFab = (FloatingActionButton) view.findViewById(R.id.center_my_location_fab);
        this.mCenterMyLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initmap();
            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        SuggestionsRepository.setmContext(getContext());
        SuggestionsRepository.setmGoogleApiClient(mGoogleApiClient);
    }

    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    protected String getMapsAPIKeyFromManifest() {
        try {
            ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            SuggestionsRepository.setmGoogleApiClient(null);
            mGoogleApiClient.disconnect();
        }
    }

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    protected int getDefaultZoom() {
        int zoom;
        if (hasWiderZoom) {
            zoom = WIDER_ZOOM;
        } else {
            zoom = DEFAULT_ZOOM;
        }
        return zoom;
    }

    protected Marker addMarker(LatLng latLng) {
        if (getMarkerIconRes() != 0)
            return mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(getMarkerIconRes())).draggable(true));
        else
            return mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
    }

    protected Marker addMarker(LatLng latLng, @DrawableRes int markerPinRes) {
        return mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(markerPinRes)).draggable(true));
    }

    protected
    @DrawableRes
    int getMarkerIconRes() {
        return mMarkerPinIconRes;
    }

    protected void startOverlayAnimation(final GroundOverlay groundOverlay) {

        AnimatorSet animatorSet = new AnimatorSet();

        ValueAnimator vAnimator = ValueAnimator.ofInt(0, 100);
        vAnimator.setRepeatCount(ValueAnimator.INFINITE);
        vAnimator.setRepeatMode(ValueAnimator.RESTART);
        vAnimator.setInterpolator(new LinearInterpolator());
        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final Integer val = (Integer) valueAnimator.getAnimatedValue();
                groundOverlay.setDimensions(val);
            }
        });

        ValueAnimator tAnimator = ValueAnimator.ofFloat(0, 1);
        tAnimator.setRepeatCount(ValueAnimator.INFINITE);
        tAnimator.setRepeatMode(ValueAnimator.RESTART);
        tAnimator.setInterpolator(new LinearInterpolator());
        tAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float val = (Float) valueAnimator.getAnimatedValue();
                groundOverlay.setTransparency(val);
            }
        });

        animatorSet.setDuration(3000);
        animatorSet.playTogether(vAnimator, tAnimator);
        animatorSet.start();
    }

    protected abstract void extractGeocode(final double lati, final double longi);

    @Override
    public void onMarkerDragStart(Marker marker) {
        Vibrator myVib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        myVib.vibrate(50);
    }

    protected Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void addOverlay(LatLng place) {
        GroundOverlay groundOverlay = mMap.addGroundOverlay(new
                GroundOverlayOptions()
                .position(place, 100)
                .transparency(0.5f)
                .zIndex(3)
                .image(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(getActivity().getResources().getDrawable(R.drawable.map_overlay)))));

        startOverlayAnimation(groundOverlay);
    }


    protected void addSingleMarkerToMap(LatLng latLng) {
        if (mMap != null) {
            if (mCurrentMapMarker != null) {
                mCurrentMapMarker.remove();
            }
            CameraPosition cameraPosition =
                    new CameraPosition.Builder().target(latLng)
                            .zoom(getDefaultZoom())
                            .build();
            hasWiderZoom = false;
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mCurrentMapMarker = addMarker(latLng);
            mMap.setOnMarkerDragListener(this);
            handleMarkerAddition(latLng);
            extractGeocode(latLng.latitude, latLng.longitude);
        }
    }

    protected abstract void handleMarkerAddition(LatLng latLng);


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.setMaxZoomPreference(20);

        if (getMapStyle() != null)
            googleMap.setMapStyle(getMapStyle());

    }

    protected MapStyleOptions getMapStyle() {
        if (mStyleEnum == NiboStyle.CUSTOM) {
            if (mStyleFileID != 0) {
                return MapStyleOptions.loadRawResourceStyle(
                        getActivity(), mStyleFileID);
            } else {
                throw new IllegalStateException("NiboStyle.CUSTOM requires that you supply a custom style file, you can get one at https://snazzymaps.com/explore");
            }
        } else if (mStyleEnum == NiboStyle.DEFAULT) {
            return null;
        } else {
            if (mStyleEnum == null) {
                return null;
            }
            {
                return MapStyleOptions.loadRawResourceStyle(
                        getActivity(), mStyleEnum.getValue());
            }
        }
    }


    protected void initmap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationRepository = new LocationRepository(getActivity(), mGoogleApiClient);

        mLocationRepository.getLocationObservable()
                .subscribe(new Consumer<Location>() {
                    @Override
                    public void accept(@NonNull Location location) throws Exception {
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .zoom(15)
                                .build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        extractGeocode(location.getLatitude(), location.getLongitude());

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });


    }

    public void setMargins(View v, int left, int top, int right, int bottom) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            v.requestLayout();
        }
    }

    @android.support.annotation.NonNull
    public String getAddressHTMLText(Address addressObj) {
        String address = addressObj.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addressObj.getLocality();
        String state = addressObj.getAdminArea();
        String country = addressObj.getCountryName();
        String postalCode = addressObj.getPostalCode();

        String part1 = addressObj.getFeatureName();
        String part2 = address + ", " + city + ", " + state + ", " + country + ", " + postalCode;

        return "<b>" + part1 + "</b><label style='color:#ccc'> <br>" + part2 + "</label>";
    }

    @android.support.annotation.NonNull
    public String getAddressString(Address addressObj) {
        String address = addressObj.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addressObj.getLocality();
        String state = addressObj.getAdminArea();
        String country = addressObj.getCountryName();
        String postalCode = addressObj.getPostalCode();

        return address + ", " + city + ", " + state + ", " + country + ", " + postalCode;
    }

    @Override
    public void onConnectionFailed(@android.support.annotation.NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onConnectionSuspended(int i) {

    }
}
