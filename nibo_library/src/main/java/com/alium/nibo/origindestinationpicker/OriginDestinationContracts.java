package com.alium.nibo.origindestinationpicker;


import com.alium.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.alium.nibo.models.NiboSelectedOriginDestination;
import com.alium.nibo.models.Route;
import com.alium.nibo.mvp.contract.NiboPresentable;
import com.alium.nibo.mvp.contract.NiboViewable;
import com.google.android.gms.location.places.Place;

import java.util.List;

/**
 * Created by aliumujib on 18/04/2018.
 */

public interface OriginDestinationContracts {

    interface Presenter extends NiboPresentable<View> {

        void findDirections(double origLatitude, double origLongitude, double destLatitude, double destLongitude);

        void getSuggestions(String query);

        void onRouteFinderSuccess(List<Route> routes);

        void onRouteFinderError(Throwable exception);

        void onGetSuggestionsSuccess(List<NiboSearchSuggestionItem> searchSuggestionItems);

        void onGetSuggestionsError(Throwable exception);

        void retryGetSuggestions();

        void getPlaceDetailsByID(NiboSearchSuggestionItem placeID);

        void onGetPlaceDetailsError(Throwable exception);

        void onGetPlaceDetailsSuccess(Place place);

        void setOriginData(NiboSearchSuggestionItem originData);

        void setDestinationData(NiboSearchSuggestionItem destinationData);

        void checkIfCompleted();

    }


    interface View extends NiboViewable<Presenter> {

        void closeSuggestions();

        void setSuggestions(List<NiboSearchSuggestionItem> niboSearchSuggestionItems);

        void clearPreviousDirections();

        void resetDirectionDetailViews();

        void setUpTimeAndDistanceText(String time, String distance);

        void setUpOriginDestinationText(String originAddress, String destinationAddress);

        void showErrorFindingRouteMessage();

        void initMapWithRoute(List<Route> routes);

        void showErrorFindingSuggestionsError();

        void setPlaceDataOrigin(Place place, NiboSearchSuggestionItem searchSuggestionItem);

        void setPlaceDataDestination(Place place, NiboSearchSuggestionItem searchSuggestionItem);

        void displayGetPlaceDetailsError();

        boolean isOriginIndicatorViewChecked();

        boolean isDestinationIndicatorViewChecked();

        void setOriginAddress(String shortTitle);

        void setDestinationAddress(String shortTitle);

        void sendResults(NiboSelectedOriginDestination selectedOriginDestination);

        void showSelectOriginMessage();

        void showSelectDestinationMessage();
    }

}
