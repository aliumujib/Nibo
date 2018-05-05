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
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;

import com.alium.nibo.R;
import com.alium.nibo.di.GoogleClientModule;
import com.alium.nibo.di.Injection;
import com.alium.nibo.di.ProviderModule;
import com.alium.nibo.models.NiboSelectedPlace;
import com.alium.nibo.mvp.contract.NiboPresentable;
import com.alium.nibo.mvp.contract.NiboViewable;
import com.alium.nibo.repo.contracts.ISuggestionRepository;
import com.alium.nibo.repo.location.LocationRepository;
import com.alium.nibo.repo.location.SuggestionsProvider;
import com.alium.nibo.utils.NiboConstants;
import com.alium.nibo.utils.NiboStyle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

public abstract class BaseNiboFragment<T extends NiboPresentable> extends Fragment implements NiboViewable<T>, OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    protected GoogleMap mMap;
    protected boolean hasWiderZoom;
    protected Marker mCurrentMapMarker;
    protected static final int DEFAULT_ZOOM = 16;
    protected static final int WIDER_ZOOM = 6;
    protected GoogleApiClient mGoogleApiClient;
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
    protected LocationRepository mLocationRepository;
    protected FloatingActionButton mCenterMyLocationFab;
    public View mOriginDestinationSeperatorLine;
    public Injection injection;
    private ISuggestionRepository suggestionsRepository;


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

    public static final String ARGS_INSTANCE = "com.moehandi.instafragment";


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

        this.mCenterMyLocationFab = (FloatingActionButton) view.findViewById(R.id.center_my_location_fab);
        this.mCenterMyLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initmap();
            }
        });

        Bundle args = getArguments();
        if (getArguments() != null) {
            mStyleEnum = (NiboStyle) args.getSerializable(NiboConstants.STYLE_ENUM_ARG);
            mStyleFileID = args.getInt(NiboConstants.STYLE_FILE_ID);
        }

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
            if (suggestionsRepository != null) {
                suggestionsRepository.stop();
            }
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
        mMap.setMyLocationEnabled(true);
        mMap.setMaxZoomPreference(20);

        if (getMapStyle() != null)
            googleMap.setMapStyle(getMapStyle());

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
                        handleLocationRetrieval(location);
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

    public class OnConnectionFailedListenerImpl implements GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(@android.support.annotation.NonNull ConnectionResult connectionResult) {

        }
    }


    public class ConnectionCallbacksImpl implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(@Nullable Bundle bundle) {

            //TODO ... enable any views that should enabled here
            suggestionsRepository = injection.getSuggestionsRepository();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    }
}
