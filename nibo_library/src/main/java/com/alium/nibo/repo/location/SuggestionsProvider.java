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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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


    public Observable<Place> getPlaceByID(final String placeId) {
        return new Observable<Place>() {
            @Override
            protected void subscribeActual(final Observer<? super Place> observer) {
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(@NonNull PlaceBuffer places) {
                                if (places.getStatus().isSuccess()) {
                                    final Place thatPlace = places.get(0);
                                    LatLng queriedLocation = thatPlace.getLatLng();
                                    Log.v("Latitude is", "" + queriedLocation.latitude);
                                    Log.v("Longitude is", "" + queriedLocation.longitude);

                                    observer.onNext(thatPlace.freeze());
                                } else {

                                }
                                places.release();
                            }
                        });
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void stop() {
        mContext = null;
        mGoogleApiClient = null;
    }


}
