package com.alium.nibo.base;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.alium.nibo.BuildConfig;
import com.alium.nibo.R;
import com.alium.nibo.di.GoogleClientModule;
import com.alium.nibo.di.Injection;
import com.alium.nibo.mvp.contract.NiboPresentable;
import com.alium.nibo.mvp.contract.NiboViewable;
import com.alium.nibo.repo.contracts.ISuggestionRepository;
import com.alium.nibo.repo.location.LocationRepository;
import com.alium.nibo.utils.NiboConstants;
import com.alium.nibo.utils.NiboStyle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.Date;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by abdulmujibaliu on 9/7/17.
 */

public abstract class BaseNiboFragment<T extends NiboPresentable> extends Fragment implements NiboViewable<T>, OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    protected GoogleMap mMap;
    protected boolean hasWiderZoom;
    protected Marker mCurrentMapMarker;
    protected static final int DEFAULT_ZOOM = 16;
    protected static final int WIDER_ZOOM = 6;


    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private LocationSettingsRequest mLocationSettingsRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;


    protected T presenter;

    protected int DEFAULT_MARKER_ICON_RES = 0;

    protected NiboStyle mStyleEnum = NiboStyle.DEFAULT;
    protected String TAG = getClass().getSimpleName();

    protected
    @RawRes
    int mStyleFileID = DEFAULT_MARKER_ICON_RES;
    protected
    @DrawableRes
    int mMarkerPinIconRes;
    protected FloatingActionButton mCenterMyLocationFab;
    public View mOriginDestinationSeperatorLine;
    public Injection injection;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();
        if (getPresenter() != null) {
            getPresenter().onStart();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }


    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public View onCreateView(@android.support.annotation.NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        //noinspection unchecked
        injectDependencies();

        if (getPresenter() != null) {
            getPresenter().attachView(this);
            getMapsAPIKeyFromManifest();
        }

        return view;
    }

    protected abstract int getLayoutId();


    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyView() {
        if (getPresenter() != null) {
            getPresenter().detachView();
        }
        super.onDestroyView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        presenter = null;
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void displayError(String message) {
        View rootContent = getActivity().findViewById(android.R.id.content);
        Snackbar.make(rootContent, message, Snackbar.LENGTH_LONG).show();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void displayError(int messageId) {
        displayError(getString(messageId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showLoading() {
        // no-op by default
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideLoading() {
        // no-op by default
    }

    @Override
    public void injectDependencies() {

        Injection.InjectionBuilder injectionBuilder = new Injection.InjectionBuilder();
        injectionBuilder.setContext(getContext());
        injectionBuilder.setGoogleClientModule(new GoogleClientModule(getAppCompatActivity(), new ConnectionCallbacksImpl(), new OnConnectionFailedListenerImpl()));
        //injectionBuilder.setProviderModule(new ProviderModule(getAppCompatActivity()));
        injection = injectionBuilder.build();

    }

    @Override
    public void attachToPresenter() {

    }

    @Override
    public void detachFromPresenter() {

    }

    @Override
    public void onLandscape() {

    }

    @Override
    public void onPortrait() {

    }

    @Override
    public void displayMessage(String message) {

    }

    @Override
    public void showNoNetwork() {

    }

    @Override
    public void close() {
        getAppCompatActivity().finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void injectPresenter(T presenter) {
        this.presenter = presenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getPresenter() {
        return presenter;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGoogleApiClient = injection.getGoogleApiClient();
        mLocationRequest = injection.getLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());

        mLocationSettingsRequest = builder.build();

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();

        this.mCenterMyLocationFab = (FloatingActionButton) view.findViewById(R.id.center_my_location_fab);
        this.mCenterMyLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMap();
            }
        });


        Bundle args = getArguments();
        if (getArguments() != null) {
            mStyleEnum = (NiboStyle) args.getSerializable(NiboConstants.STYLE_ENUM_ARG);
            mStyleFileID = args.getInt(NiboConstants.STYLE_FILE_ID);
        }

    }



    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    extractGeocode(location.getLatitude(), location.getLongitude());


                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(15)
                            .build();
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    handleLocationRetrieval(location);
                    extractGeocode(location.getLatitude(), location.getLongitude());
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getAppCompatActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    protected void showSnackbar(final int mainTextStringId, final int actionStringId,
                                View.OnClickListener listener) {
        if (getView() != null) {
            Snackbar.make(
                    getAppCompatActivity().findViewById(android.R.id.content),
                    getString(mainTextStringId),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(actionStringId), listener).show();
        }
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getAppCompatActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @android.support.annotation.NonNull String[] permissions,
                                           @android.support.annotation.NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getAppCompatActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");


                        //noinspection MissingPermission
                        if (checkPermissions()) {
                            mMap.setMyLocationEnabled(true);
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, Looper.myLooper());
                        }

                    }
                })
                .addOnFailureListener(getAppCompatActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@android.support.annotation.NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                displayError(errorMessage);
                        }
                    }
                });
    }

    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public void getMapsAPIKeyFromManifest() {
        try {
            ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String apiKey = bundle.getString("com.google.android.geo.API_KEY");
            getPresenter().setGoogleAPIKey(apiKey);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        if (getPresenter() != null) {
            getPresenter().onStop();
        }
        super.onStop();
    }

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }


    @Override
    public void connectGoogleApiClient() {
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


    protected abstract void handleLocationRetrieval(Location latLng);


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMaxZoomPreference(20);

        if (getMapStyle() != null) {
            googleMap.setMapStyle(getMapStyle());
        }
    }


    protected MapStyleOptions getMapStyle() {
        if (mStyleEnum == NiboStyle.CUSTOM) {
            if (mStyleFileID != DEFAULT_MARKER_ICON_RES) {
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


    protected void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void setMargins(View v, int left, int top, int right, int bottom) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            v.requestLayout();
        }
    }

    public class OnConnectionFailedListenerImpl implements GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }
    }


    public class ConnectionCallbacksImpl implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            //TODO ... enable any views that should enabled here


        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    }

}
