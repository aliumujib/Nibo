package com.alium.nibo.di;

import com.alium.nibo.domain.directions.FindDirectionsUseCase;
import com.alium.nibo.domain.geocoding.GeocodeCordinatesUseCase;
import com.alium.nibo.domain.places.GetPlaceDetailsUseCase;
import com.alium.nibo.domain.places.GetPlaceSuggestionsUseCase;

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

    public FindDirectionsUseCase getFindDirectionsUseCase(){
        return new FindDirectionsUseCase(repositoryModule.getDirectionsRepository());
    }


    public GetPlaceSuggestionsUseCase getPlaceSuggestionsUseCase(){
        return new GetPlaceSuggestionsUseCase(providerModule.getSuggestionsProvider());
    }
}
