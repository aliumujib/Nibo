package com.alium.nibo.origindestinationpicker;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alium.nibo.R;
import com.alium.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.alium.nibo.base.BaseNiboFragment;
import com.alium.nibo.lib.BottomSheetBehaviorGoogleMapsLike;
import com.alium.nibo.models.NiboSelectedOriginDestination;
import com.alium.nibo.origindestinationpicker.adapter.NiboBaseOrigDestSuggestionAdapter;
import com.alium.nibo.models.Route;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.maps.model.JointType.ROUND;

/**
 * A placeholder fragment containing a simple view.
 */
public class NiboOriginDestinationPickerFragment extends BaseNiboFragment<OriginDestinationContracts.Presenter> implements OriginDestinationContracts.View {

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


    private ArrayList<LatLng> mListLatLng = new ArrayList<>();

    private Marker mOriginMapMarker;
    private Marker mDestinationMarker;
    private Polyline mPrimaryPolyLine;
    private Polyline mSecondaryPolyLine;

    private TextView mTimeTaken;
    private TextView mOriginToDestinationTv;
    private LinearLayout mTimeDistanceLL;
    private LinearLayout mSuggestionsLL;
    private String mOriginEditTextHint;
    private String mDestinationEditTextHint;
    private int mOriginMarkerPinIconRes;
    private int mDestinationMarkerPinIconRes;
    private int mBackButtonIconRes;
    private int mTextFieldClearIconRes;
    private int mPrimaryPolyLineColor;
    private int mSecondaryPolyLineColor;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSearchSuggestions = new ArrayList<>();
        mSearchItemAdapter = new NiboBaseOrigDestSuggestionAdapter(getContext(), mSearchSuggestions);

        initView(view);
        initListeners();


        Bundle args;
        if ((args = getArguments()) != null) {

            mMarkerPinIconRes = args.getInt(NiboConstants.MARKER_PIN_ICON_RES);

            mOriginEditTextHint = args.getString(NiboConstants.ORIGIN_EDIT_TEXT_HINT_ARG);
            mDestinationEditTextHint = args.getString(NiboConstants.DEST_EDIT_TEXT_HINT_ARG);

            mOriginMarkerPinIconRes = args.getInt(NiboConstants.ORIGIN_MARKER_ICON_RES_ARG);
            mDestinationMarkerPinIconRes = args.getInt(NiboConstants.DEST_MARKER_ICON_RES_ARG);

            mBackButtonIconRes = args.getInt(NiboConstants.BACK_BUTTON_ICON_RES_ARG);
            mTextFieldClearIconRes = args.getInt(NiboConstants.TEXT_FIELD_CLEAR_ICON_RES_ARG);

            mPrimaryPolyLineColor = args.getInt(NiboConstants.PRIMARY_POLY_LINE_COLOR_RES);
            mSecondaryPolyLineColor = args.getInt(NiboConstants.SECONDARY_POLY_LINE_COLOR_RES);

        }

        if (mOriginEditTextHint != null) {
            mOriginEditText.setHint(mOriginEditTextHint);
        }

        if (mDestinationEditTextHint != null) {
            mDestinationEditText.setHint(mDestinationEditTextHint);
        }

        if (mBackButtonIconRes != 0) {
            mToolbar.setNavigationIcon(mBackButtonIconRes);
        }

        if (mTextFieldClearIconRes != 0) {
            mOriginEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, mTextFieldClearIconRes, 0);
            mDestinationEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, mTextFieldClearIconRes, 0);
        }

        initMap();


    }

    public static NiboOriginDestinationPickerFragment newInstance(String originEditTextHint, String destinationEditTextHint, NiboStyle mapStyle,
                                                                  int styleFileID, int originMarkerPinIconRes, int destinationMarkerPinIconRes, int backButtonIconRes,
                                                                  int textFieldClearIconRes, int primaryPolyLineColor, int secondaryPolyLineColor) {

        Bundle args = new Bundle();
        args.putString(NiboConstants.ORIGIN_EDIT_TEXT_HINT_ARG, originEditTextHint);
        args.putString(NiboConstants.DEST_EDIT_TEXT_HINT_ARG, destinationEditTextHint);
        args.putSerializable(NiboConstants.STYLE_ENUM_ARG, mapStyle);
        args.putInt(NiboConstants.STYLE_FILE_ID, styleFileID);

        args.putInt(NiboConstants.ORIGIN_MARKER_ICON_RES_ARG, originMarkerPinIconRes);
        args.putInt(NiboConstants.DEST_MARKER_ICON_RES_ARG, destinationMarkerPinIconRes);
        args.putInt(NiboConstants.BACK_BUTTON_ICON_RES_ARG, backButtonIconRes);
        args.putInt(NiboConstants.TEXT_FIELD_CLEAR_ICON_RES_ARG, textFieldClearIconRes);

        args.putInt(NiboConstants.PRIMARY_POLY_LINE_COLOR_RES, primaryPolyLineColor);
        args.putInt(NiboConstants.SECONDARY_POLY_LINE_COLOR_RES, secondaryPolyLineColor);

        NiboOriginDestinationPickerFragment fragment = new NiboOriginDestinationPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public NiboOriginDestinationPickerFragment() {
    }

    @Override
    protected void extractGeocode(double lati, double longi) {

    }

    @Override
    protected void handleLocationRetrieval(Location latLng) {

    }


    private void sendRequest(LatLng origin, LatLng destination) {
        presenter.findDirections(origin.latitude, origin.longitude, destination.latitude, destination.longitude);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_origin_destination_picker;
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
        this.mSuggestionsListView = (ListView) convertView.findViewById(R.id.suggestions_list);
        this.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
        this.mDoneFab = (FloatingActionButton) convertView.findViewById(R.id.done_fab);
        this.mTimeDistanceLL = (LinearLayout) convertView.findViewById(R.id.title_distance_ll);
        this.mSuggestionsLL = (LinearLayout) convertView.findViewById(R.id.suggestions_progress_ll);
        this.mTimeTaken = (TextView) convertView.findViewById(R.id.time_taken);
        this.mOriginToDestinationTv = (TextView) convertView.findViewById(R.id.origin_to_destination_tv);
        this.mOriginDestinationSeperatorLine = convertView.findViewById(R.id.orig_dest_seperator_line);

        //this.mRoundedIndicatorDestination.setChecked(true);
        this.mSuggestionsListView.setAdapter(mSearchItemAdapter);

        this.mOriginEditText.setOnTouchListener(getClearListener(mOriginEditText));
        this.mDestinationEditText.setOnTouchListener(getClearListener(mDestinationEditText));

        this.mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getActivity().finish();
                return false;
            }
        });

        this.mDoneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               presenter.checkIfCompleted();
            }
        });

        //((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);


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
                        hideKeyboard();
                        Log.d(TAG, "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        mDoneFab.animate().scaleX(0).scaleY(0).setDuration(300).start();
                        mCenterMyLocationFab.animate().scaleX(0).scaleY(0).setDuration(300).start();

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

        this.mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);


    }

    @Override
    public void showSelectOriginMessage() {
        displayError("Please select an initial location");
    }

    @Override
    public void showSelectDestinationMessage() {
        displayError("Please select a final location");
    }

    @Override
    public void sendResults(NiboSelectedOriginDestination selectedOriginDestination) {
        Intent intent = new Intent();
        intent.putExtra(NiboConstants.SELECTED_ORIGIN_DESTINATION_RESULT, selectedOriginDestination);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
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
                        editText.clearFocus();
                        return true;
                    }
                }
                return false;
            }
        };
    }

    @Override
    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mDestinationEditText.setFocusableInTouchMode(true);
        mOriginEditText.setFocusableInTouchMode(true);
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
                        mDestinationEditText.setFocusableInTouchMode(false);
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
                        mOriginEditText.setFocusableInTouchMode(false);
                        findResults(s);

                    }
                });

        mSuggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mRoundedIndicatorOrigin.isChecked()) {
                    presenter.setOriginData(mSearchSuggestions.get(position));
                    presenter.getPlaceDetailsByID(mSearchSuggestions.get(position));
                } else if (mRoundedIndicatorDestination.isChecked()) {
                    presenter.setDestinationData(mSearchSuggestions.get(position));
                    presenter.getPlaceDetailsByID(mSearchSuggestions.get(position));
                }
            }
        });
    }

    @Override
    public void setOriginAddress(String shortTitle) {
        mOriginEditText.setText(shortTitle);
    }


    @Override
    public void setDestinationAddress(String shortTitle) {
        mDestinationEditText.setText(shortTitle);
    }

    @Override
    public void injectDependencies() {
        super.injectDependencies();

        presenter = injection.getOriginDestinationPickerPresenter();
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

            if (mOriginMarkerPinIconRes != DEFAULT_MARKER_ICON_RES) {
                mOriginMapMarker = addMarker(latLng, mOriginMarkerPinIconRes);
            } else {
                mOriginMapMarker = addMarker(latLng);
            }


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
            if (mDestinationMarkerPinIconRes != DEFAULT_MARKER_ICON_RES) {
                mDestinationMarker = addMarker(latLng, mDestinationMarkerPinIconRes);
            } else {
                mDestinationMarker = addMarker(latLng);
            }

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

    @Override
    public void closeSuggestions() {
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
        presenter.getSuggestions(s);
    }


    @Override
    public void setSuggestions(List<NiboSearchSuggestionItem> niboSearchSuggestionItems) {
        mSearchSuggestions.clear();
        mSearchSuggestions.addAll(niboSearchSuggestionItems);
        mSearchItemAdapter.notifyDataSetChanged();
    }

    private Observable<String> getObservableForEditext(EditText editText) {
        return RxTextView.textChanges(editText).filter(new Predicate<CharSequence>() {
            @Override
            public boolean test(@NonNull CharSequence charSequence) throws Exception {
                return charSequence.length() > 3;
            }
        }).debounce(300, TimeUnit.MILLISECONDS).map(new Function<CharSequence, String>() {
            @Override
            public String apply(@NonNull CharSequence charSequence) throws Exception {
                return charSequence.toString();
            }
        });
    }

    @Override
    public void clearPreviousDirections() {
        Log.d(TAG, "STARTED");

        if (mPrimaryPolyLine != null) {
            mPrimaryPolyLine.remove();
        }

        if (mSecondaryPolyLine != null) {
            mSecondaryPolyLine.remove();
        }

        this.mListLatLng.clear();
    }

    @Override
    public void resetDirectionDetailViews() {
        mOriginToDestinationTv.setText("");
        mTimeTaken.setText("");
    }

    @Override
    public void setUpTimeAndDistanceText(String time, String distance) {
        mTimeTaken.setText(String.format(getString(R.string.time_distance), time, distance));
    }

    @Override
    public void setUpOriginDestinationText(String originAddress, String destinationAddress) {
        mOriginToDestinationTv.setText(String.format(getString(R.string.origin_to_dest_text), originAddress, destinationAddress));
    }


    private void drawPolyline(final List<Route> routes) {

        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = new PolylineOptions();

        mCoordinatorlayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOriginEditText.clearFocus();
                mDestinationEditText.clearFocus();
                mCoordinatorlayout.requestLayout();
            }
        }, 1000);

        for (int i = 0; i < routes.size(); i++) {
            this.mListLatLng.addAll(routes.get(i).points);
        }

        lineOptions.width(10);
        if (mPrimaryPolyLineColor == 0) {
            lineOptions.color(Color.BLACK);
        } else {
            lineOptions.color(ContextCompat.getColor(getContext(), mPrimaryPolyLineColor));
        }
        lineOptions.startCap(new SquareCap());
        lineOptions.endCap(new SquareCap());
        lineOptions.jointType(ROUND);
        mPrimaryPolyLine = mMap.addPolyline(lineOptions);

        PolylineOptions greyOptions = new PolylineOptions();
        greyOptions.width(10);
        if (mSecondaryPolyLineColor == 0) {
            greyOptions.color(Color.GRAY);
        } else {
            lineOptions.color(ContextCompat.getColor(getContext(), mSecondaryPolyLineColor));
        }
        greyOptions.startCap(new SquareCap());
        greyOptions.endCap(new SquareCap());
        greyOptions.jointType(ROUND);
        mSecondaryPolyLine = mMap.addPolyline(greyOptions);

        animatePolyLine();
    }

    private void animatePolyLine() {

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                List<LatLng> latLngList = mPrimaryPolyLine.getPoints();
                int initialPointSize = latLngList.size();
                int animatedValue = (int) animator.getAnimatedValue();
                int newPoints = (animatedValue * mListLatLng.size()) / 100;

                if (initialPointSize < newPoints) {
                    latLngList.addAll(mListLatLng.subList(initialPointSize, newPoints));
                    mPrimaryPolyLine.setPoints(latLngList);
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

            List<LatLng> primaryLatLng = mPrimaryPolyLine.getPoints();
            List<LatLng> secondaryLatLng = mSecondaryPolyLine.getPoints();

            secondaryLatLng.clear();
            secondaryLatLng.addAll(primaryLatLng);
            primaryLatLng.clear();

            mPrimaryPolyLine.setPoints(primaryLatLng);
            mSecondaryPolyLine.setPoints(secondaryLatLng);

            mPrimaryPolyLine.setZIndex(2);

            animator.start();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {


        }
    };


    @Override
    public void showErrorFindingRouteMessage() {
        displayError("Error finding routes, please try another set.");
    }

    @Override
    public void showErrorFindingSuggestionsError() {
        showSnackbar(R.string.error_finding_suggestions, R.string.retry_title, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.retryGetSuggestions();
            }
        });
    }

    @Override
    public void setPlaceDataOrigin(Place place, NiboSearchSuggestionItem searchSuggestionItem) {
        addOriginMarker(place.getLatLng());
    }

    @Override
    public void setPlaceDataDestination(Place place, NiboSearchSuggestionItem searchSuggestionItem) {
        addDestinationMarker(place.getLatLng());
    }

    @Override
    public void displayGetPlaceDetailsError() {

    }

    @Override
    public boolean isOriginIndicatorViewChecked() {
        return mRoundedIndicatorOrigin.isChecked();
    }

    @Override
    public boolean isDestinationIndicatorViewChecked() {
        return mRoundedIndicatorDestination.isChecked();
    }

    @Override
    public void initMapWithRoute(List<Route> routes) {
        Log.d(TAG, "DONE");
        if (!routes.isEmpty()) {
            drawPolyline(routes);
        }
        if (mBehavior.getState() == (BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED) && mTimeDistanceLL.getVisibility() == View.INVISIBLE) {
            toggleViews();
        }
    }
}
