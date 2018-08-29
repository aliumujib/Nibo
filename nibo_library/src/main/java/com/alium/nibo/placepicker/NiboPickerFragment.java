package com.alium.nibo.placepicker;

import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.transition.TransitionManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alium.nibo.R;
import com.alium.nibo.autocompletesearchbar.NiboAutocompleteSVProvider;
import com.alium.nibo.autocompletesearchbar.NiboPlacesAutoCompleteSearchView;
import com.alium.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.alium.nibo.base.BaseNiboFragment;
import com.alium.nibo.models.NiboSelectedPlace;
import com.alium.nibo.utils.NiboConstants;
import com.alium.nibo.utils.NiboStyle;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class NiboPickerFragment extends BaseNiboFragment<NiboPickerContracts.Presenter> implements NiboAutocompleteSVProvider, NiboPickerContracts.View {

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


    @Override
    public void setPlaceData(Place place) {
        addSingleMarkerToMap(place.getLatLng());
    }

    @Override
    public void displayPlaceDetailsError() {
        displayError("Error details");
    }

    @Override
    public boolean isSearching() {
        return mSearchView.isSearching();
    }

    @Override
    public void closeSearch() {
        mSearchView.closeSearch();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_picker_nibo;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setUpBackPresses(view);
        initMap();


        if (mConfirmButtonTitle != null && !mConfirmButtonTitle.equals("")) {
            mPickLocationTextView.setText(mConfirmButtonTitle);
        }

        if (mSearchBarTitle != null && !mSearchBarTitle.equals("")) {
            mSearchView.setLogoText(mSearchBarTitle);
        }


        mPickLocationLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.sendResults();
            }
        });

        //  geoCodingRepository = injection.getGeoCodingRepository();
    }


    private void setUpBackPresses(View view) {
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    presenter.handleBackPress();
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
        if (presenter != null) {
            presenter.getGeocode(lati, longi);
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
        this.mRootLayout = convertView.findViewById(R.id.root_layout);
        this.mSearchView = (NiboPlacesAutoCompleteSearchView) convertView.findViewById(R.id.searchview);
        this.mLocationDetails = (LinearLayout) convertView.findViewById(R.id.location_details);
        this.mGeocodeAddress = (TextView) convertView.findViewById(R.id.geocode_address);
        this.mPickLocationTextView = (TextView) convertView.findViewById(R.id.pick_location_textview);
        this.mPickLocationLL = (LinearLayout) convertView.findViewById(R.id.pick_location_btn);
        this.mSearchView.setmProvider(this);
    }

    @Override
    public void onStop() {

        super.onStop();
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
        return new SearchListenerImpl();
    }

    @Override
    public boolean getShouldUseVoice() {
        return false;
    }

    @Override
    public void setResults(NiboSelectedPlace niboSelectedPlace) {
        Intent intent = new Intent();
        intent.putExtra(NiboConstants.SELECTED_PLACE_RESULT, niboSelectedPlace);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void setGeocodeAddress(String address) {
        mGeocodeAddress.setText(address);
    }

    @Override
    public void injectDependencies() {
        super.injectDependencies();
        injectPresenter(injection.getNiboPickerPresenter());
    }

    @Override
    public void showResultView() {
        showAddressWithTransition();
    }

    @Override
    public void displayErrorGeoCodingMessage() {
        displayError("Error getting address for your location");
    }


    class SearchListenerImpl implements NiboPlacesAutoCompleteSearchView.SearchListener {

        @Override
        public void onSearchEditOpened() {
            hideAddressWithTransition();
        }

        @Override
        public void onSearchEditClosed() {
            showAddressWithTransition();
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
            initSearchViews(niboSearchSuggestionItem);
            presenter.getPlaceDetailsById(niboSearchSuggestionItem.getPlaceID());
            return false;
        }

        @Override
        public void onSearchCleared() {

        }

    }

    public void initSearchViews(NiboSearchSuggestionItem niboSearchSuggestionItem) {
        mSearchView.setSearchString(niboSearchSuggestionItem.getFullTitle(), true);
        mSearchView.setLogoText(niboSearchSuggestionItem.getFullTitle());
        mSearchView.closeSearch();
        hideKeyboard();
        hideAddressWithTransition();
    }

}
