package com.alium.nibo.placepicker;

import android.util.Log;

import com.alium.nibo.domain.Params;
import com.alium.nibo.domain.geocoding.GeocodeCordinatesUseCase;
import com.alium.nibo.domain.places.GetPlaceDetailsUseCase;
import com.alium.nibo.models.NiboSelectedPlace;
import com.alium.nibo.mvp.BaseNiboPresenter;
import com.alium.nibo.placepicker.NiboPickerContracts.View;
import com.alium.nibo.rx.DefaultObserver;
import com.alium.nibo.utils.NiboConstants;
import com.google.android.gms.location.places.Place;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class NiboPickerPresenter extends BaseNiboPresenter<View> implements NiboPickerContracts.Presenter {

    private final String TAG = getClass().getSimpleName();
    protected NiboSelectedPlace selectedPlace;

    protected Params geoCodingParams = Params.create();

    private GeocodeCordinatesUseCase geocodeCordinatesUseCase;
    private GetPlaceDetailsUseCase getPlaceDetailsUseCase;

    public NiboPickerPresenter(GeocodeCordinatesUseCase geocodeCordinatesUseCase, GetPlaceDetailsUseCase getPlaceDetailsUseCase) {
        this.geocodeCordinatesUseCase = geocodeCordinatesUseCase;
        this.getPlaceDetailsUseCase = getPlaceDetailsUseCase;
    }

    @Override
    public void onViewCreated() {
        super.onViewCreated();

    }


    @Override
    public void sendResults() {
        getView().setResults(selectedPlace);
    }

    @Override
    public void setGoogleAPIKey(String apiKey) {
        geoCodingParams.putString(NiboConstants.API_KEY_PARAM, apiKey);
    }

    @Override
    public void onStop() {
        super.onStop();
        geocodeCordinatesUseCase.dispose();
    }

    @Override
    public void getGeocode(double lati, double longi) {
        geoCodingParams.putData(NiboConstants.LATITUDE_PARAM, lati);
        geoCodingParams.putData(NiboConstants.LONGITUDE_PARAM, longi);
        geocodeCordinatesUseCase.execute(new GeocodeAddressObserver(), geoCodingParams);
    }


    class GeocodeAddressObserver extends DefaultObserver<String> {
        @Override
        public void onNext(String address) {
            super.onNext(address);
            onGeocodeSuccess(address);
        }


        @Override
        public void onError(Throwable exception) {
            super.onError(exception);
            onGeocodeError(exception);
        }
    }

    class PlaceDetailsObserver extends DefaultObserver<Place> {
        @Override
        public void onNext(Place place) {
            super.onNext(place);
            onGetPlaceDetailsSuccess(place);
        }


        @Override
        public void onError(Throwable exception) {
            super.onError(exception);
            onGetPlaceDetailsError(exception);
        }
    }

    @Override
    public void onGetPlaceDetailsError(Throwable exception) {
        Log.e(TAG, exception.getMessage(), exception);
        getView().displayPlaceDetailsError();
    }


    @Override
    public void onGetPlaceDetailsSuccess(Place place) {
        getView().setPlaceData(place);
    }

    @Override
    public void onGeocodeError(Throwable exception) {
        Log.e(TAG, exception.getLocalizedMessage(), exception);
        getView().displayErrorGeoCodingMessage();
    }

    @Override
    public void onGeocodeSuccess(String address) {
        double latitude = geoCodingParams.getDouble(NiboConstants.LATITUDE_PARAM, 0);
        double longitude = geoCodingParams.getDouble(NiboConstants.LONGITUDE_PARAM, 0);
        selectedPlace = new NiboSelectedPlace(latitude, longitude, null, address);
        getView().showResultView();
        getView().setGeocodeAddress(address);
        Log.d(TAG, address);
    }

    @Override
    public void getPlaceDetailsById(String placeID) {
        Params getPlaceDetailsParams = Params.create();
        getPlaceDetailsParams.putData(NiboConstants.PLACE_ID_PARAM, placeID);
        getPlaceDetailsUseCase.execute(new PlaceDetailsObserver(), getPlaceDetailsParams);
    }

    @Override
    public void handleBackPress() {
        if(getView().isSearching()){
            getView().closeSearch();
        }else {
            getView().close();
        }
    }

}
