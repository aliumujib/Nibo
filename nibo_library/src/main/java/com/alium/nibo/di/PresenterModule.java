package com.alium.nibo.di;

import com.alium.nibo.domain.geocoding.GeocodeCordinatesUseCase;
import com.alium.nibo.origindestinationpicker.OriginDestinationContracts;
import com.alium.nibo.origindestinationpicker.OriginDestinationPickerPresenter;
import com.alium.nibo.placepicker.NiboPickerContracts;
import com.alium.nibo.placepicker.NiboPickerPresenter;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class PresenterModule {

    InteractorModule interactorModule;

    public PresenterModule(InteractorModule interactorModule) {
        this.interactorModule = interactorModule;
    }


    public NiboPickerContracts.Presenter getNiboPickerPresenter() {
        return new NiboPickerPresenter(interactorModule.getGeocodeCordinatesUseCase(), interactorModule.getGetPlaceDetailsUseCase());
    }

    public OriginDestinationContracts.Presenter getOriginDestinationPickerPresenter() {
        return new OriginDestinationPickerPresenter(interactorModule.getFindDirectionsUseCase(), interactorModule.getPlaceSuggestionsUseCase(), interactorModule.getGetPlaceDetailsUseCase());
    }
}
