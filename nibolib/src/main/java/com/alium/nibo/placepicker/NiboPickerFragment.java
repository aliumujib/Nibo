package com.alium.nibo.placepicker;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.text.Html;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alium.nibo.R;
import com.alium.nibo.autocompletesearchbar.NiboAutocompleteSVProvider;
import com.alium.nibo.autocompletesearchbar.NiboPlacesAutoCompleteSearchView;
import com.alium.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.alium.nibo.base.BaseNiboFragment;
import com.alium.nibo.models.NiboSelectedPlace;
import com.alium.nibo.repo.location.LocationAddress;
import com.alium.nibo.utils.NiboConstants;
import com.alium.nibo.utils.NiboStyle;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class NiboPickerFragment extends BaseNiboFragment implements NiboAutocompleteSVProvider {

    private Address mGeolocation;
    private RelativeLayout mRootLayout;
    private NiboPlacesAutoCompleteSearchView mSearchView;
    private LinearLayout mLocationDetails;
    private TextView mGeocodeAddress;
    private TextView mPickLocationTextView;

    protected String mSearchBarTitle;
    protected String mConfirmButtonTitle;
    private LinearLayout mPickLocationLL;

    public NiboPickerFragment() {

    }

    public static NiboPickerFragment newInstance(String searchBarTitle, String confirmButtonTitle, NiboStyle styleEnum, @RawRes int styleFileID, @DrawableRes int markerIconRes) {
        Bundle args = new Bundle();
        NiboPickerFragment fragment = new NiboPickerFragment();
        args.putString(NiboConstants.SEARCHBAR_TITLE_ARG, searchBarTitle);
        args.putString(NiboConstants.SELECTION_BUTTON_TITLE, confirmButtonTitle);
        args.putSerializable(NiboConstants.STYLE_ENUM_ARG, styleEnum);
        args.putInt(NiboConstants.STYLE_FILE_ID, styleFileID);
        args.putInt(NiboConstants.MARKER_PIN_ICON_RES, markerIconRes);
        fragment.setArguments(args);
        return fragment;
    }


    protected void getPlaceDetailsByID(String placeId) {
        mLocationRepository.getPlaceByID(placeId).subscribe(new Consumer<Place>() {
            @Override
            public void accept(@NonNull Place place) throws Exception {
                addSingleMarkerToMap(place.getLatLng());
            }
        }
        , new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                Log.e(TAG, throwable.getMessage());
            }
        }

        );
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_picker_nibo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setUpBackPresses(view);
        initmap();


        if (mConfirmButtonTitle != null && !mConfirmButtonTitle.equals("")) {
            mPickLocationTextView.setText(mConfirmButtonTitle);
        }

        if (mSearchBarTitle != null && !mSearchBarTitle.equals("")) {
            mSearchView.setLogoText(mSearchBarTitle);
        }


        mPickLocationLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(NiboConstants.RESULTS_SELECTED, mCurrentSelection);
                getActivity().setResult(RESULT_OK, intent);
                getActivity().finish();
            }
        });

    }


    private void setUpBackPresses(View view) {
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    if (mSearchView.isSearching()) {
                        mSearchView.closeSearch();
                    }
                    return true;
                }
                return false;
            }
        });
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
            extractGeocode(latLng.latitude, latLng.longitude);
        }
    }

    @Override
    protected void extractGeocode(final double lati, final double longi) {

        LatLng currentPosition = new LatLng(lati, longi);
        //addSingleMarkerToMap(currentPosition);

        if ((String.valueOf(lati).equals(null))) {
            Toast.makeText(getContext(), "Invlaid location", Toast.LENGTH_SHORT).show();
        } else {
            LocationAddress.sharedLocationAddressInstance.getObservableAddressFromLocation(lati, longi,
                    getContext())
                    .subscribe(new Consumer<Address>() {
                        @Override
                        public void accept(@NonNull Address address) throws Exception {
                            mGeolocation = address;
                            if (mGeolocation != null) {
                                String fullAddress = getAddressHTMLText(address);
                                mGeocodeAddress.setText(Html.fromHtml(fullAddress));
                                showAddressWithTransition();
                                Log.d("mGeolocation", " " + fullAddress);
                                mCurrentSelection = new NiboSelectedPlace(new LatLng(lati, longi), null, getAddressString(address));
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            Log.e(TAG, throwable.getMessage());
                        }
                    });

        }
    }

    @Override
    protected void handleLocationRetrieval(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        addOverlay(latLng);
        addSingleMarkerToMap(latLng);
    }


    void showAddressWithTransition() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(mRootLayout);
        }
        mLocationDetails.setVisibility(View.VISIBLE);
        mMap.setPadding(0, 0, 0, mLocationDetails.getHeight());

    }


    void hideAddressWithTransition() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(mRootLayout);
        }
        mLocationDetails.setVisibility(View.GONE);
        mMap.setPadding(0, 0, 0, 0);

    }


    @Override
    public void onMarkerDragStart(Marker marker) {
        super.onMarkerDragStart(marker);
        hideAddressWithTransition();
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        extractGeocode(marker.getPosition().latitude, marker.getPosition().longitude);
    }

    private void initView(View convertView) {
        this.mRootLayout = (RelativeLayout) convertView.findViewById(R.id.root_layout);
        this.mSearchView = (NiboPlacesAutoCompleteSearchView) convertView.findViewById(R.id.searchview);
        this.mLocationDetails = (LinearLayout) convertView.findViewById(R.id.location_details);
        this.mGeocodeAddress = (TextView) convertView.findViewById(R.id.geocode_address);
        this.mPickLocationTextView = (TextView) convertView.findViewById(R.id.pick_location_textview);
        this.mPickLocationLL = (LinearLayout) convertView.findViewById(R.id.pick_location_btn);

        mSearchView.setmProvider(this);
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

    }

    @Override
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onHomeButtonClicked() {
        getActivity().finish();
    }

    @Override
    public NiboPlacesAutoCompleteSearchView.SearchListener getSearchListener() {
        return new NiboPlacesAutoCompleteSearchView.SearchListener() {


            @Override
            public void onSearchEditOpened() {
                hideAddressWithTransition();


            }

            @Override
            public void onSearchEditClosed() {
                if (mGeolocation != null) {
                    showAddressWithTransition();
                }

            }


            @Override
            public boolean onSearchEditBackPressed() {
                if (mSearchView.isEditing()) {
                    mSearchView.cancelEditing();
                    return true;
                }
                return false;
            }

            @Override
            public void onSearchExit() {

            }

            @Override
            public void onSearchTermChanged(String term) {


            }

            @Override
            public void onSearch(String string) {

            }

            @Override
            public boolean onSuggestion(NiboSearchSuggestionItem niboSearchSuggestionItem) {
                Toast.makeText(getContext(), niboSearchSuggestionItem.getValue(), Toast.LENGTH_LONG).show();
                mSearchView.setSearchString(niboSearchSuggestionItem.getFullTitle(), true);
                mSearchView.setLogoText(niboSearchSuggestionItem.getFullTitle());
                getPlaceDetailsByID(niboSearchSuggestionItem.getValue());
                mSearchView.closeSearch();
                hideKeyboard();
                hideAddressWithTransition();
                return false;
            }

            @Override
            public void onSearchCleared() {

            }

        };
    }

    @Override
    public boolean getShouldUseVoice() {
        return false;
    }
}
