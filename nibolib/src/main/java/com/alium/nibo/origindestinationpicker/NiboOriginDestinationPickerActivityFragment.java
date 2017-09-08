package com.alium.nibo.origindestinationpicker;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.alium.nibo.R;
import com.alium.nibo.base.BaseNiboFragment;
import com.alium.nibo.lib.BottomSheetBehaviorGoogleMapsLike;
import com.alium.nibo.repo.location.SuggestionsRepository;
import com.alium.nibo.utils.NiboConstants;
import com.alium.nibo.utils.NiboStyle;
import com.alium.nibo.utils.customviews.RoundedView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.cryse.widget.persistentsearch.SearchItem;
import org.cryse.widget.persistentsearch.SearchItemAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * A placeholder fragment containing a simple view.
 */
public class NiboOriginDestinationPickerActivityFragment extends BaseNiboFragment {

    private CoordinatorLayout mCoordinatorlayout;
    private Toolbar mToolbar;
    private RoundedView mRoundedIndicatorOrigin;
    private RoundedView mRoundedIndicatorDestination;
    private EditText mOriginEditText;
    private EditText mDestinationEditText;
    private NestedScrollView mBottomSheet;
    private CardView mContentCardView;
    private ListView mSuggestionsListView;

    private SearchItemAdapter mSearchItemAdapter;
    private ArrayList<SearchItem> mSearchSuggestions;
    private BottomSheetBehaviorGoogleMapsLike<View> mBehavior;

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

        Bundle args;
        if ((args = getArguments()) != null) {
            mConfirmButtonTitle = args.getString(NiboConstants.SELECTION_BUTTON_TITLE);
            mStyleEnum = (NiboStyle) args.getSerializable(NiboConstants.STYLE_ENUM_ARG);
            mMarkerPinIconRes = args.getInt(NiboConstants.MARKER_PIN_ICON_RES);
            mStyleFileID = args.getInt(NiboConstants.STYLE_FILE_ID);
        }

        mSearchSuggestions = new ArrayList<>();
        mSearchItemAdapter = new SearchItemAdapter(getContext(), mSearchSuggestions);


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

        this.mRoundedIndicatorDestination.setChecked(true);

        /**
         * If we want to listen for states callback
         */
        View bottomSheet = mCoordinatorlayout.findViewById(R.id.bottom_sheet);

        mBehavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        mBehavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        addMargins();
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        removeMargins();
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);


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
                    addMargins();
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
                    addMargins();
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
                        findResults(s);
                    }
                });

        destinationObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull String s) throws Exception {
                        findResults(s);
                    }
                });

        mSuggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mRoundedIndicatorOrigin.isChecked()) {
                    mOriginEditText.setText(mSearchSuggestions.get(position).getTitle());
                    closeSuggestions();
                } else if (mRoundedIndicatorDestination.isChecked()) {
                    mDestinationEditText.setText(mSearchSuggestions.get(position).getTitle());
                    closeSuggestions();
                }
            }
        });
    }

    private void closeSuggestions() {
        mSearchSuggestions.clear();
        mSearchItemAdapter.clear();
        hideKeyboard(mCoordinatorlayout);
        mBehavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
    }

    private void findResults(String s) {
        SuggestionsRepository.sharedInstance.getSuggestions(s).subscribe(new Consumer<Collection<SearchItem>>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull Collection<SearchItem> searchItems) throws Exception {
                mSearchSuggestions.clear();
                mSearchSuggestions.addAll(searchItems);
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
}
