package com.alium.nibo.di;

import com.alium.nibo.domain.geocoding.GeocodeCordinatesUseCase;
import com.alium.nibo.domain.places.GetPlaceDetailsUseCase;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class InteractorModule {

    RepositoryModule repositoryModule;

    public InteractorModule(RepositoryModule repositoryModule) {
        this.repositoryModule = repositoryModule;
    }


    public GeocodeCordinatesUseCase getGeocodeCordinatesUseCase() {
        return new GeocodeCordinatesUseCase(repositoryModule.getGeoCodingRepository());
    }


    public GetPlaceDetailsUseCase getGetPlaceDetailsUseCase() {
        return new GetPlaceDetailsUseCase(repositoryModule.getDirectionsRepository());
    }

}
