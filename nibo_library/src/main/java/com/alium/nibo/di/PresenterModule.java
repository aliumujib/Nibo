package com.alium.nibo.di;

import com.alium.nibo.domain.geocoding.GeocodeCordinatesUseCase;
import com.alium.nibo.placepicker.NiboPickerPresenter;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class PresenterModule {

    InteractorModule interactorModule;

    public PresenterModule(InteractorModule interactorModule) {
        this.interactorModule = interactorModule;
    }


    public NiboPickerPresenter getNiboPickerPresenter() {
        return new NiboPickerPresenter(interactorModule.getGeocodeCordinatesUseCase(), interactorModule.getGetPlaceDetailsUseCase());
    }


}
