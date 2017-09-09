package com.alium.nibo.repo.location;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.alium.nibo.R;
import com.alium.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;

/**
 * Created by abdulmujibaliu on 9/8/17.
 */

public class SuggestionsRepository {


    private String TAG = getClass().getSimpleName();
    private static GoogleApiClient mGoogleApiClient;
    private static Context mContext;

    public static SuggestionsRepository sharedInstance = new SuggestionsRepository();

    public static void setmContext(Context mContext) {
        SuggestionsRepository.mContext = mContext;
    }

    public static void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        SuggestionsRepository.mGoogleApiClient = mGoogleApiClient;
    }

    public Observable<Collection<NiboSearchSuggestionItem>> getSuggestions(final String query) {
        final List<NiboSearchSuggestionItem> placeSuggestionItems = new ArrayList<>();

        return new Observable<Collection<NiboSearchSuggestionItem>>() {
            @Override
            protected void subscribeActual(final Observer<? super Collection<NiboSearchSuggestionItem>> observer) {
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
                                                NiboSearchSuggestionItem placeSuggestion = new NiboSearchSuggestionItem(
                                                        prediction.getFullText(null).toString(),
                                                        prediction.getPlaceId(), NiboSearchSuggestionItem.TYPE_SEARCH_ITEM_SUGGESTION,
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
