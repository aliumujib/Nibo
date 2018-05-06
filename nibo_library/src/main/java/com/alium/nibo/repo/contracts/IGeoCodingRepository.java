package com.alium.nibo.repo.contracts;

import io.reactivex.Observable;

/**
 * Created by aliumujib on 03/05/2018.
 */

public interface IGeoCodingRepository {

    Observable<String> getObservableAddressStringFromLatLng(final double latitude, final double longitude, String apiKey);

}
