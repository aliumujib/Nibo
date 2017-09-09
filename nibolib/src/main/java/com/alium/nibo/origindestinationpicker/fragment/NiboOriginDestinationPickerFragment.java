package com.alium.nibo.origindestinationpicker.fragment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alium.nibo.R;
import com.alium.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.alium.nibo.base.BaseNiboFragment;
import com.alium.nibo.lib.BottomSheetBehaviorGoogleMapsLike;
import com.alium.nibo.origindestinationpicker.adapter.NiboBaseOrigDestSuggestionAdapter;
import com.alium.nibo.repo.directions.DirectionFinder;
import com.alium.nibo.repo.directions.DirectionFinderListener;
import com.alium.nibo.repo.directions.Route;
import com.alium.nibo.repo.location.SuggestionsRepository;
import com.alium.nibo.utils.NiboConstants;
import com.alium.nibo.utils.NiboStyle;
import com.alium.nibo.utils.customviews.RoundedView;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static com.google.android.gms.maps.model.JointType.ROUND;

/**
 * A placeholder fragment containing a simple view.
 */
public class NiboOriginDestinationPickerFragment extends BaseNiboFragment implements DirectionFinderListener {

    private CoordinatorLayout mCoordinatorlayout;
    private Toolbar mToolbar;
    private RoundedView mRoundedIndicatorOrigin;
    private RoundedView mRoundedIndicatorDestination;
    private EditText mOriginEditText;
    private EditText mDestinationEditText;
    private NestedScrollView mBottomSheet;
    private CardView mContentCardView;
    private ListView mSuggestionsListView;
    private ProgressBar mProgressBar;
    private NiboBaseOrigDestSuggestionAdapter mSearchItemAdapter;
    private ArrayList<NiboSearchSuggestionItem> mSearchSuggestions;
    private BottomSheetBehaviorGoogleMapsLike<View> mBehavior;
    private FloatingActionButton mDoneFab;

    private Marker mOriginMapMarker;
    private Marker mDestinationMarker;

    private ArrayList<LatLng> listLatLng = new ArrayList<>();
    private Polyline blackPolyLine;
    private Polyline greyPolyLine;
    private TextView mTimeTaken;
    private TextView mOriginToDestinationTv;
    private LinearLayout mTimeDistanceLL;
    private LinearLayout mSuggestionsLL;
    private NiboSearchSuggestionItem mOriginSuggestion;
    private NiboSearchSuggestionItem mDestinationSuggestion;


    public NiboOriginDestinationPickerFragment() {
    }

    @Override
    protected void extractGeocode(double lati, double longi) {

    }

    @Override
    protected void handleMarkerAddition(LatLng latLng) {

    }


    private void sendRequest(LatLng origin, LatLng destination) {
        try {
            String destinationString = "" + destination.latitude + "," + destination.longitude;
            String originString = "" + origin.latitude + ","
                    + origin.longitude;
            new DirectionFinder(getMapsAPIKeyFromManifest(), this, originString, destinationString).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args;
        if ((args = getArguments()) != null) {
            mConfirmButtonTitle = args.getString(NiboConstants.SELECTION_BUTTON_TITLE);
            mStyleEnum = (NiboStyle) args.getSerializable(NiboConstants.STYLE_ENUM_ARG);
            mMarkerPinIconRes = args.getInt(NiboConstants.MARKER_PIN_ICON_RES);
            mStyleFileID = args.getInt(NiboConstants.STYLE_FILE_ID);
        }

        mSearchSuggestions = new ArrayList<>();
        mSearchItemAdapter = new NiboBaseOrigDestSuggestionAdapter(getContext(), mSearchSuggestions);


        initView(view);
        initListeners();

        initmap();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_origin_destination_picker, container, false);
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

    private void initView(View convertView) {

        this.mCoordinatorlayout = (CoordinatorLayout) convertView.findViewById(R.id.coordinatorlayout);
        this.mToolbar = (Toolbar) convertView.findViewById(R.id.toolbar);
        this.mRoundedIndicatorOrigin = (RoundedView) convertView.findViewById(R.id.rounded_indicator_source);
        this.mRoundedIndicatorDestination = (RoundedView) convertView.findViewById(R.id.rounded_indicator_destination);
        this.mOriginEditText = (EditText) convertView.findViewById(R.id.origin_edit_text);
        this.mDestinationEditText = (EditText) convertView.findViewById(R.id.destination_edit_text);
        this.mBottomSheet = (NestedScrollView) convertView.findViewById(R.id.bottom_sheet);
        this.mContentCardView = (CardView) convertView.findViewById(R.id.content_card_view);
        this.mSuggestionsListView = (ListView) convertView.findViewById(R.id.suggestions_recyclerview);
        this.mSuggestionsListView.setAdapter(mSearchItemAdapter);
        this.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
        this.mDoneFab = (FloatingActionButton) convertView.findViewById(R.id.done_fab);
        this.mTimeDistanceLL = (LinearLayout) convertView.findViewById(R.id.title_distance_ll);
        this.mSuggestionsLL = (LinearLayout) convertView.findViewById(R.id.suggestions_progress_ll);
        this.mTimeTaken = (TextView) convertView.findViewById(R.id.time_taken);
        this.mOriginToDestinationTv = (TextView) convertView.findViewById(R.id.origin_to_destination_tv);

        //this.mRoundedIndicatorDestination.setChecked(true);

        this.mOriginEditText.setOnTouchListener(getClearListener(mOriginEditText));
        this.mDestinationEditText.setOnTouchListener(getClearListener(mDestinationEditText));


        /**
         * we want to listen for states
         */
        View bottomSheet = mCoordinatorlayout.findViewById(R.id.bottom_sheet);

        mBehavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        mBehavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        if (mTimeDistanceLL.getVisibility() != View.VISIBLE) {
                            toggleViews();
                        }
                        mDoneFab.animate().scaleX(1).scaleY(1).setDuration(300).start();
                        mCenterMyLocationFab.animate().scaleX(1).scaleY(1).setDuration(300).start();
                        Log.d(TAG, "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        mDoneFab.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        Log.d(TAG, "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d(TAG, "STATE_EXPANDED");
                        mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        if (mSuggestionsLL.getVisibility() != View.VISIBLE) {
                            toggleViews();
                        }

                        mDoneFab.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        mCenterMyLocationFab.animate().scaleX(0).scaleY(0).setDuration(300).start();

                        mSearchSuggestions.clear();
                        mSearchItemAdapter.clear();
                        mSearchItemAdapter.notifyDataSetChanged();
                        Log.d(TAG, "STATE_ANCHOR_POINT");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d(TAG, "STATE_HIDDEN");
                        break;
                    default:
                        Log.d(TAG, "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);


    }

    @NonNull
    private View.OnTouchListener getClearListener(final EditText editText) {
        return new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = editText.getRight()
                            - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        // clicked on clear icon
                        editText.setText("");
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }


    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void removeMargins() {
        mContentCardView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    TransitionManager.beginDelayedTransition(mContentCardView);
                }
                setMargins(mContentCardView, 0, 0, 0, 0);
            }
        }, 200);
    }

    private void addMargins() {
        mContentCardView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    TransitionManager.beginDelayedTransition(mContentCardView);
                }
                setMargins(mContentCardView, dpToPx(16), 0, dpToPx(16), 0);
            }
        }, 200);
    }


    private void initListeners() {
        mOriginEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mRoundedIndicatorOrigin.setChecked(true);
                    mRoundedIndicatorDestination.setChecked(false);
                    mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
                } else {
                    mRoundedIndicatorOrigin.setChecked(false);
                }
            }
        });

        mDestinationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mRoundedIndicatorDestination.setChecked(true);
                    mRoundedIndicatorOrigin.setChecked(false);
                    mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
                } else {
                    mRoundedIndicatorDestination.setChecked(false);
                }
            }
        });


        Observable<String> originObservable = getObservableForEditext(mOriginEditText);
        Observable<String> destinationObservable = getObservableForEditext(mDestinationEditText);

        originObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull String s) throws Exception {
                        if (mBehavior.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED) {
                            mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
                        }
                        findResults(s);
                    }
                });

        destinationObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull String s) throws Exception {
                        if (mBehavior.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED) {
                            mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
                        }
                        findResults(s);

                    }
                });

        mSuggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mRoundedIndicatorOrigin.isChecked()) {
                    mOriginSuggestion = mSearchSuggestions.get(position);
                    mOriginEditText.setText(mSearchSuggestions.get(position).getShortTitle());
                    getPlaceDetailsByID(mSearchSuggestions.get(position).getValue());
                } else if (mRoundedIndicatorDestination.isChecked()) {
                    mDestinationSuggestion = mSearchSuggestions.get(position);
                    mDestinationEditText.setText(mSearchSuggestions.get(position).getShortTitle());
                    getPlaceDetailsByID(mSearchSuggestions.get(position).getValue());
                }
            }
        });
    }

    protected void getPlaceDetailsByID(String placeId) {
        mLocationRepository.getPlaceByID(placeId).subscribe(new Consumer<Place>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull Place place) throws Exception {
                hideLoading();
                closeSuggestions();
                if (mRoundedIndicatorOrigin.isChecked()) {
                    addOriginMarker(place.getLatLng());


                }
                if (mRoundedIndicatorDestination.isChecked()) {
                    addDestinationMarker(place.getLatLng());
                }
            }
        });
    }


    protected void toggleVisibility(View... views) {
        for (View view : views) {
            boolean isVisible = view.getVisibility() == View.VISIBLE;
            view.setVisibility(isVisible ? View.INVISIBLE : View.VISIBLE);
        }
    }

    void toggleViews() {
        toggleVisibility(mSuggestionsLL);
        toggleVisibility(mTimeDistanceLL);
    }

    void addOriginMarker(LatLng latLng) {
        if (mMap != null) {
            if (mOriginMapMarker != null) {
                mOriginMapMarker.remove();
                mOriginMapMarker = null;
            }
            CameraPosition cameraPosition =
                    new CameraPosition.Builder().target(latLng)
                            .zoom(getDefaultZoom())
                            .build();

            hasWiderZoom = false;
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            mOriginMapMarker = addMarker(latLng);
            mMap.setOnMarkerDragListener(this);

            showBothMarkersAndGetDirections();
        }
    }


    void addDestinationMarker(LatLng latLng) {
        if (mMap != null) {
            if (mDestinationMarker != null) {
                mDestinationMarker.remove();
                mDestinationMarker = null;
            }
            hasWiderZoom = false;
            mDestinationMarker = addMarker(latLng);

            showBothMarkersAndGetDirections();
        }
    }


    private void showBothMarkersAndGetDirections() {
        if (mOriginMapMarker != null && mDestinationMarker != null) {
            int width = getResources().getDisplayMetrics().widthPixels;
            int padding = (int) (width * 0.10); //
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(getLatLngBoundsForMarkers(), padding);
            mMap.moveCamera(cu);
            mMap.animateCamera(cu);

            sendRequest(mOriginMapMarker.getPosition(), mDestinationMarker.getPosition());

        } else {

        }
    }


    LatLngBounds getLatLngBoundsForMarkers() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mOriginMapMarker.getPosition());
        builder.include(mDestinationMarker.getPosition());
        return builder.build();
    }


    private void closeSuggestions() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideKeyboard();
                mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                mCoordinatorlayout.requestLayout();
            }
        });
    }

    private void findResults(String s) {
        showLoading();
        SuggestionsRepository.sharedInstance.getSuggestions(s).subscribe(new Consumer<Collection<NiboSearchSuggestionItem>>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull Collection<NiboSearchSuggestionItem> niboSearchSuggestionItems) throws Exception {
                mSearchSuggestions.clear();
                mSearchSuggestions.addAll(niboSearchSuggestionItems);
                mSearchItemAdapter.notifyDataSetChanged();
            }
        });
    }

    private Observable<String> getObservableForEditext(EditText editText) {
        return RxTextView.textChanges(editText).filter(new Predicate<CharSequence>() {
            @Override
            public boolean test(@io.reactivex.annotations.NonNull CharSequence charSequence) throws Exception {
                return charSequence.length() > 3;
            }
        }).debounce(300, TimeUnit.MILLISECONDS).map(new Function<CharSequence, String>() {
            @Override
            public String apply(@io.reactivex.annotations.NonNull CharSequence charSequence) throws Exception {
                return charSequence.toString();
            }
        });
    }

    @Override
    public void onDirectionFinderStart() {
        Log.d(TAG, "STARTED");

        if (blackPolyLine != null) {
            blackPolyLine.remove();
        }

        if (greyPolyLine != null) {
            greyPolyLine.remove();
        }

        this.listLatLng.clear();

        showLoading();

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route) {
        hideLoading();
        Log.d(TAG, "DONE");
        if (!route.isEmpty()) {
            drawPolyline(route);
        }
        if (mBehavior.getState() == (BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED) && mTimeDistanceLL.getVisibility() == View.INVISIBLE) {
            toggleViews();
        }
    }

    private void setUpTimeAndDistanceText(String time, String distance) {
        mTimeTaken.setText(String.format(getString(R.string.time_distance), time, distance));
    }

    private void setUpOriginDestinationText() {
        if (mOriginSuggestion != null && mDestinationSuggestion != null) {
            mOriginToDestinationTv.setText(String.format(getString(R.string.origin_to_dest_text), mOriginSuggestion.getShortTitle(), mDestinationSuggestion.getShortTitle()));
        }
    }

    @Override
    public void onDirectionFinderError(String errorMessage) {
        hideLoading();
        Log.d(TAG, errorMessage);
    }


    void drawPolyline(final List<Route> routes) {

        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = new PolylineOptions();

        mCoordinatorlayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOriginEditText.clearFocus();
                mDestinationEditText.clearFocus();
                setUpTimeAndDistanceText(routes.get(0).distance.text, routes.get(0).duration.text);
                setUpOriginDestinationText();
                mCoordinatorlayout.requestLayout();
            }
        }, 1000);

        for (int i = 0; i < routes.size(); i++) {
            this.listLatLng.addAll(routes.get(i).points);
        }

        lineOptions.width(10);
        lineOptions.color(Color.BLACK);
        lineOptions.startCap(new SquareCap());
        lineOptions.endCap(new SquareCap());
        lineOptions.jointType(ROUND);
        blackPolyLine = mMap.addPolyline(lineOptions);

        PolylineOptions greyOptions = new PolylineOptions();
        greyOptions.width(10);
        greyOptions.color(Color.GRAY);
        greyOptions.startCap(new SquareCap());
        greyOptions.endCap(new SquareCap());
        greyOptions.jointType(ROUND);
        greyPolyLine = mMap.addPolyline(greyOptions);

        animatePolyLine();
    }

    private void animatePolyLine() {

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                List<LatLng> latLngList = blackPolyLine.getPoints();
                int initialPointSize = latLngList.size();
                int animatedValue = (int) animator.getAnimatedValue();
                int newPoints = (animatedValue * listLatLng.size()) / 100;

                if (initialPointSize < newPoints) {
                    latLngList.addAll(listLatLng.subList(initialPointSize, newPoints));
                    blackPolyLine.setPoints(latLngList);
                }


            }
        });

        animator.addListener(polyLineAnimationListener);
        animator.start();

    }

    Animator.AnimatorListener polyLineAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {

            List<LatLng> blackLatLng = blackPolyLine.getPoints();
            List<LatLng> greyLatLng = greyPolyLine.getPoints();

            greyLatLng.clear();
            greyLatLng.addAll(blackLatLng);
            blackLatLng.clear();

            blackPolyLine.setPoints(blackLatLng);
            greyPolyLine.setPoints(greyLatLng);

            blackPolyLine.setZIndex(2);

            animator.start();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {


        }
    };


}
