package com.alium.nibo.origindestinationpicker;

import android.util.Log;

import com.alium.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.alium.nibo.domain.Params;
import com.alium.nibo.domain.directions.FindDirectionsUseCase;
import com.alium.nibo.domain.places.GetPlaceDetailsUseCase;
import com.alium.nibo.domain.places.GetPlaceSuggestionsUseCase;
import com.alium.nibo.models.NiboSelectedOriginDestination;
import com.alium.nibo.models.Route;
import com.alium.nibo.mvp.BaseNiboPresenter;
import com.alium.nibo.rx.DefaultObserver;
import com.alium.nibo.utils.NiboConstants;
import com.google.android.gms.location.places.Place;

import java.util.List;

/**
 * Created by aliumujib on 06/05/2018.
 */

public class OriginDestinationPickerPresenter extends BaseNiboPresenter<OriginDestinationContracts.View> implements OriginDestinationContracts.Presenter {

    private final String TAG = getClass().getName();
    private Params directionParams = Params.create();
    private Params findSuggestionParams = Params.create();
    private Params getPlaceDetailsParams = Params.create();

    private FindDirectionsUseCase findDirectionsUseCase;
    private GetPlaceSuggestionsUseCase getPlaceSuggestionsUseCase;
    private GetPlaceDetailsUseCase getPlaceDetailsUseCase;


    private NiboSearchSuggestionItem mOriginSuggestion;
    private NiboSearchSuggestionItem mDestinationSuggestion;
    private NiboSelectedOriginDestination mSelectedOriginDestination = new NiboSelectedOriginDestination();


    public OriginDestinationPickerPresenter(FindDirectionsUseCase findDirectionsUseCase,
                                            GetPlaceSuggestionsUseCase getPlaceSuggestionsUseCase,
                                            GetPlaceDetailsUseCase getPlaceDetailsUseCase) {
        this.findDirectionsUseCase = findDirectionsUseCase;
        this.getPlaceSuggestionsUseCase = getPlaceSuggestionsUseCase;
        this.getPlaceDetailsUseCase = getPlaceDetailsUseCase;
    }

    @Override
    public void setGoogleAPIKey(String apiKey) {
        super.setGoogleAPIKey(apiKey);
        directionParams.putString(NiboConstants.API_KEY_PARAM, apiKey);
    }

    @Override
    public void findDirections(double origLatitude, double origLongitude, double destLatitude, double destLongitude) {
        String destinationString = "" + origLatitude + "," + origLongitude;
        String originString = "" + destLatitude + ","
                + destLongitude;
        directionParams.putString(NiboConstants.ORIGIN_PARAM, originString);
        directionParams.putString(NiboConstants.DESTINATION_PARAM, destinationString);
        getView().clearPreviousDirections();
        getView().showLoading();

        findDirectionsUseCase.execute(new RouteObserver(), directionParams);
    }

    @Override
    public void getSuggestions(String query) {
        getView().showLoading();
        findSuggestionParams.putString(NiboConstants.SUGGESTION_QUERY_PARAM, query);
        getPlaceSuggestionsUseCase.execute(new SuggestionsObserver(), findSuggestionParams);
    }

    @Override
    public void onRouteFinderSuccess(List<Route> routes) {
        getView().hideLoading();
        getView().resetDirectionDetailViews();
        getView().setUpOriginDestinationText(mOriginSuggestion.getShortTitle(), mDestinationSuggestion.getShortTitle());
        getView().setUpTimeAndDistanceText(routes.get(0).distance.text, routes.get(0).duration.text);
        getView().initMapWithRoute(routes);
    }

    @Override
    public void onRouteFinderError(Throwable exception) {
        Log.d(TAG, exception.getMessage(), exception);
        getView().resetDirectionDetailViews();
        getView().hideLoading();
        getView().showErrorFindingRouteMessage();
    }

    @Override
    public void onStop() {
        super.onStop();
        findDirectionsUseCase.dispose();
        getPlaceSuggestionsUseCase.dispose();
        getPlaceDetailsUseCase.dispose();
    }

    @Override
    public void onGetSuggestionsSuccess(List<NiboSearchSuggestionItem> searchSuggestionItems) {
        getView().hideLoading();
        getView().setSuggestions(searchSuggestionItems);
    }

    @Override
    public void onGetSuggestionsError(Throwable exception) {
        Log.d(TAG, exception.getMessage(), exception);
        getView().showErrorFindingSuggestionsError();
        getView().hideLoading();
    }

    @Override
    public void retryGetSuggestions() {
        getPlaceSuggestionsUseCase.execute(new SuggestionsObserver(), findSuggestionParams);
    }

    @Override
    public void getPlaceDetailsByID(NiboSearchSuggestionItem searchSuggestionItem) {
        getPlaceDetailsParams.putData(NiboConstants.SUGGESTION_ITEM_PARAM, searchSuggestionItem);
        getPlaceDetailsParams.putData(NiboConstants.PLACE_ID_PARAM, searchSuggestionItem.getPlaceID());
        getPlaceDetailsUseCase.execute(new PlaceDetailsObserver(), getPlaceDetailsParams);
    }


    @Override
    public void onGetPlaceDetailsError(Throwable exception) {
        getView().hideLoading();
        getView().closeSuggestions();
        Log.e(TAG, exception.getMessage(), exception);
        getView().displayGetPlaceDetailsError();
    }


    @Override
    public void onGetPlaceDetailsSuccess(Place place) {
        getView().hideLoading();
        getView().closeSuggestions();
        NiboSearchSuggestionItem searchSuggestionItem = (NiboSearchSuggestionItem) getPlaceDetailsParams
                .getData(NiboConstants.SUGGESTION_ITEM_PARAM, null);
        if (getView().isOriginIndicatorViewChecked()) {
            mSelectedOriginDestination.setOriginItem(searchSuggestionItem);
            mSelectedOriginDestination.setOriginLatLng(place.getLatLng());
            getView().setPlaceDataOrigin(place, searchSuggestionItem);
        } else if (getView().isDestinationIndicatorViewChecked()) {
            mSelectedOriginDestination.setDestinationItem(searchSuggestionItem);
            mSelectedOriginDestination.setDestinationLatLng(place.getLatLng());
            getView().setPlaceDataDestination(place, searchSuggestionItem);
        }
    }

    @Override
    public void setOriginData(NiboSearchSuggestionItem originData) {
        mOriginSuggestion = originData;
        mSelectedOriginDestination.setOriginItem(null);
        mSelectedOriginDestination.setOriginLatLng(null);
        getView().setOriginAddress(originData.getShortTitle());
    }

    @Override
    public void setDestinationData(NiboSearchSuggestionItem destinationData) {
        mDestinationSuggestion = destinationData;
        mSelectedOriginDestination.setDestinationItem(null);
        mSelectedOriginDestination.setDestinationLatLng(null);
        getView().setDestinationAddress(destinationData.getShortTitle());
    }

    @Override
    public void checkIfCompleted() {
        if (!mSelectedOriginDestination.isOriginValid()) {
            getView().showSelectOriginMessage();
        } else if (!mSelectedOriginDestination.isDestinationValid()) {
            getView().showSelectDestinationMessage();
        } else {
            getView().sendResults(mSelectedOriginDestination);
        }
    }


    class RouteObserver extends DefaultObserver<List<Route>> {

        @Override
        public void onNext(List<Route> routes) {
            super.onNext(routes);
            onRouteFinderSuccess(routes);
        }

        @Override
        public void onError(Throwable exception) {
            super.onError(exception);
            onRouteFinderError(exception);
        }
    }

    class SuggestionsObserver extends DefaultObserver<List<NiboSearchSuggestionItem>> {

        @Override
        public void onNext(List<NiboSearchSuggestionItem> searchSuggestionItems) {
            super.onNext(searchSuggestionItems);
            onGetSuggestionsSuccess(searchSuggestionItems);
        }

        @Override
        public void onError(Throwable exception) {
            super.onError(exception);
            onGetSuggestionsError(exception);
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

}
