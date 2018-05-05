package com.alium.nibo.repo.location;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.alium.nibo.R;
import com.alium.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.alium.nibo.repo.contracts.ISuggestionRepository;
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

public class SuggestionsProvider implements ISuggestionRepository {


    private String TAG = getClass().getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public SuggestionsProvider(GoogleApiClient mGoogleApiClient, Context mContext) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.mContext = mContext;
    }

    public Observable<Collection<NiboSearchSuggestionItem>> getSuggestions(final String query) {
        final List<NiboSearchSuggestionItem> placeSuggestionItems = new ArrayList<>();

        if (mGoogleApiClient == null) {
            Log.d(TAG, "Google play services cannot be null");
        }

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
                                                        mContext.getResources().getDrawable(R.drawable.ic_map_marker_def)
                                                );

                                                placeSuggestionItems.add(placeSuggestion);
                                            }

                                            observer.onNext(placeSuggestionItems);

                                        } else {
                                            Log.d(TAG, buffer.toString());
                                            observer.onError(new Throwable(buffer.getStatus().getStatusMessage()));
                                        }
                                        //Prevent memory leak by releasing buffer
                                        buffer.release();
                                    }
                                }, 60, TimeUnit.SECONDS);

            }
        };
    }

    @Override
    public void stop() {
        mContext = null;
        mGoogleApiClient = null;
    }


}
