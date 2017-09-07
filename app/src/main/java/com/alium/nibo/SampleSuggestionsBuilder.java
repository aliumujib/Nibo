package com.alium.nibo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;

import org.cryse.widget.persistentsearch.SearchItem;
import org.cryse.widget.persistentsearch.SearchSuggestionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;

public class SampleSuggestionsBuilder implements SearchSuggestionsBuilder {
    private Context mContext;
    private List<SearchItem> mHistorySuggestions = new ArrayList<SearchItem>();
    final List<SearchItem> mPlaceSuggestionItems = new ArrayList<>();


    private String TAG = getClass().getSimpleName();
    private GoogleApiClient mGoogleApiClient;

    public SampleSuggestionsBuilder(Context context) {
        this.mContext = context;

    }


    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }


    @Override
    public Collection<SearchItem> buildEmptySearchSuggestion(int maxCount) {
        List<SearchItem> items = new ArrayList<SearchItem>();
        items.addAll(mHistorySuggestions);
        return items;
    }

    @Override
    public Collection<SearchItem> buildSearchSuggestion(int maxCount, String query) {

        Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query, null, null)
                .setResultCallback(
                        new ResultCallback<AutocompletePredictionBuffer>() {
                            @Override
                            public void onResult(@NonNull AutocompletePredictionBuffer buffer) {
                                mPlaceSuggestionItems.clear();
                                if (buffer.getStatus().isSuccess()) {
                                    Log.d(TAG, buffer.toString() + " " + buffer.getCount());

                                    for (AutocompletePrediction prediction : buffer) {
                                        Log.d(TAG, prediction.getFullText(null).toString());
                                        //Add as a new item to avoid IllegalArgumentsException when buffer is released
                                        SearchItem placeSuggestion = new SearchItem(
                                                prediction.getFullText(null).toString(),
                                                null, SearchItem.TYPE_SEARCH_ITEM_SUGGESTION
                                        );

                                        mPlaceSuggestionItems.add(placeSuggestion);
                                    }

                                } else {
                                    Log.d(TAG, buffer.toString());
                                }
                                //Prevent memory leak by releasing buffer
                                buffer.release();
                            }
                        }, 60, TimeUnit.SECONDS);

        return mPlaceSuggestionItems;
    }

    @Override
    public Observable<Collection<SearchItem>> rXbuildSearchSuggestion(int maxCount, final String query) {
        final List<SearchItem> placeSuggestionItems = new ArrayList<>();

        return new Observable<Collection<SearchItem>>() {
            @Override
            protected void subscribeActual(final Observer<? super Collection<SearchItem>> observer) {
                Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, query, null, null)
                        .setResultCallback(
                                new ResultCallback<AutocompletePredictionBuffer>() {
                                    @Override
                                    public void onResult(@NonNull AutocompletePredictionBuffer buffer) {
                                        placeSuggestionItems.clear();
                                        if (buffer.getStatus().isSuccess()) {
                                            Log.d(TAG, buffer.toString() + " " + buffer.getCount());

                                            for (AutocompletePrediction prediction : buffer) {
                                                Log.d(TAG, prediction.getFullText(null).toString());
                                                //Add as a new item to avoid IllegalArgumentsException when buffer is released
                                                SearchItem placeSuggestion = new SearchItem(
                                                        prediction.getFullText(null).toString(),
                                                        null, SearchItem.TYPE_SEARCH_ITEM_SUGGESTION,
                                                        mContext.getResources().getDrawable(R.drawable.ic_map_marker_grey600_18dp)
                                                );

                                                placeSuggestionItems.add(placeSuggestion);
                                            }

                                            observer.onNext(placeSuggestionItems);

                                        } else {
                                            Log.d(TAG, buffer.toString());
                                        }
                                        //Prevent memory leak by releasing buffer
                                        buffer.release();
                                    }
                                }, 60, TimeUnit.SECONDS);

            }
        };
    }
}
