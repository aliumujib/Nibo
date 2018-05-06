package com.alium.nibo.di;

import android.content.Context;

import com.alium.nibo.repo.location.LocationRepository;
import com.alium.nibo.repo.location.SuggestionsProvider;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class ProviderModule {

    private final GoogleApiClient googleApiClient;
    private final Context context;

    private SuggestionsProvider suggestionsProvider;
    private LocationRepository locationRepository;

    public ProviderModule(GoogleApiClient googleApiClient, Context context) {
        this.googleApiClient = googleApiClient;
        this.context = context;
    }

    public SuggestionsProvider getSuggestionsProvider() {
        if (suggestionsProvider == null) {
            suggestionsProvider = new SuggestionsProvider(googleApiClient, context);
        }
        return suggestionsProvider;
    }

    public LocationRepository getLocationRepository() {
        if (locationRepository == null) {
            locationRepository = new LocationRepository(googleApiClient, context);
        }
        return locationRepository;
    }

}
