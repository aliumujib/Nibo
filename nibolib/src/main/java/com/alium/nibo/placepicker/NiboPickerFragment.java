package com.alium.nibo.placepicker;

import android.animation.Animator;
import android.content.Intent;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.design.widget.FloatingActionButton;
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
import com.alium.nibo.base.BaseNiboFragment;
import com.alium.nibo.models.NiboSelectedPlace;
import com.alium.nibo.repo.location.LocationAddress;
import com.alium.nibo.utils.NiboConstants;
import com.alium.nibo.utils.NiboStyle;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.cryse.widget.persistentsearch.DefaultVoiceRecognizerDelegate;
import org.cryse.widget.persistentsearch.RxPersistentSearchView;
import org.cryse.widget.persistentsearch.SearchItem;
import org.cryse.widget.persistentsearch.SearchSuggestionsBuilder;
import org.cryse.widget.persistentsearch.VoiceRecognitionDelegate;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class NiboPickerFragment extends BaseNiboFragment {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 200;
    private Address mGeolocation;
    private RelativeLayout mRootLayout;
    private RxPersistentSearchView mSearchView;
    private FloatingActionButton mCenterMyLocationFab;
    private LinearLayout mLocationDetails;
    private TextView mGeocodeAddress;
    private View mSearchTintView;
    private String TAG = getClass().getSimpleName();
    private SearchSuggestionsBuilder mSamplesSuggestionsBuilder;

    private TextView mPickLocationTextView;


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

    public void setUpSearchView(boolean setUpVoice) {

        if (setUpVoice) {
            VoiceRecognitionDelegate delegate = new DefaultVoiceRecognizerDelegate(this, VOICE_RECOGNITION_REQUEST_CODE);
            if (delegate.isVoiceRecognitionAvailable()) {
                mSearchView.setVoiceRecognitionDelegate(delegate);
            }
        }

        // Hamburger has been clicked
        mSearchView.setHomeButtonListener(new RxPersistentSearchView.HomeButtonListener() {
            @Override
            public void onHomeButtonClick() {
                getActivity().finish();
            }
        });
        mSamplesSuggestionsBuilder = new PlaceSuggestionsBuilder(getActivity());

        mSearchView.setSuggestionBuilder(mSamplesSuggestionsBuilder);

        mSearchView.setSearchListener(new RxPersistentSearchView.SearchListener() {


            @Override
            public void onSearchEditOpened() {
                hideAddressWithTransition();

                //Use this to tint the screen
                if (mSearchTintView != null) {
                    mSearchTintView.setVisibility(View.VISIBLE);
                    mSearchTintView
                            .animate()
                            .alpha(1.0f)
                            .setDuration(300)
                            .setListener(new SimpleAnimationListener())
                            .start();
                }


            }

            @Override
            public void onSearchEditClosed() {
                if (mGeolocation != null) {
                    showAddressWithTransition();
                }

                if (mSearchTintView != null) {
                    mSearchTintView
                            .animate()
                            .alpha(0.0f)
                            .setDuration(300)
                            .setListener(new SimpleAnimationListener() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mSearchTintView.setVisibility(View.GONE);
                                }
                            })
                            .start();
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
            public boolean onSuggestion(SearchItem searchItem) {
                Toast.makeText(getContext(), searchItem.getValue(), Toast.LENGTH_LONG).show();
                mSearchView.setSearchString(searchItem.getTitle(), true);
                mSearchView.setLogoText(searchItem.getTitle());
                getPlaceDetailsByID(searchItem.getValue());
                mSearchView.closeSearch();
                hideKeyboard();
                hideAddressWithTransition();
                return false;
            }

            @Override
            public void onSearchCleared() {

            }

        });

    }

    private void getPlaceDetailsByID(String placeId) {
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@android.support.annotation.NonNull PlaceBuffer places) {
                        if (places.getStatus().isSuccess()) {
                            if (places.getCount() > 0) {
                                setNewMapMarker(places.get(0).getLatLng());
                            }
                        }
                        places.release();
                    }
                });
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


        mPickLocationTextView.setOnClickListener(new View.OnClickListener() {
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


    @Override
    protected void extractGeocode(final double lati, final double longi) {
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
                    });

        }
    }

    @Override
    protected void handleMarkerAddition(LatLng latLng) {
        addOverlay(latLng);
        hideAddressWithTransition();
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
        this.mSearchView = (RxPersistentSearchView) convertView.findViewById(R.id.searchview);
        this.mCenterMyLocationFab = (FloatingActionButton) convertView.findViewById(R.id.center_my_location_fab);
        this.mLocationDetails = (LinearLayout) convertView.findViewById(R.id.location_details);
        this.mGeocodeAddress = (TextView) convertView.findViewById(R.id.geocode_address);
        this.mSearchTintView = convertView.findViewById(R.id.view_search_tint);
        this.mPickLocationTextView = (TextView) convertView.findViewById(R.id.pick_location_btn);

        mCenterMyLocationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initmap();
            }
        });

        setUpSearchView(false);
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

    }
}
