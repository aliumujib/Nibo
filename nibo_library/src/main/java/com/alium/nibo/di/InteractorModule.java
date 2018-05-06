package com.alium.nibo.di;

import com.alium.nibo.domain.geocoding.GeocodeCordinatesUseCase;
import com.alium.nibo.domain.places.GetPlaceDetailsUseCase;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class InteractorModule {

    RepositoryModule repositoryModule;
    private ProviderModule providerModule;

    public InteractorModule(RepositoryModule repositoryModule, ProviderModule providerModule) {
        this.repositoryModule = repositoryModule;
        this.providerModule = providerModule;
    }


    public GeocodeCordinatesUseCase getGeocodeCordinatesUseCase() {
        return new GeocodeCordinatesUseCase(repositoryModule.getGeoCodingRepository());
    }


    public GetPlaceDetailsUseCase getGetPlaceDetailsUseCase() {
        return new GetPlaceDetailsUseCase(providerModule.getSuggestionsProvider());
    }

}
