package com.alium.nibo.domain.geocoding;

import com.alium.nibo.domain.Params;
import com.alium.nibo.domain.base.BaseUseCase;
import com.alium.nibo.repo.contracts.IGeoCodingRepository;
import com.alium.nibo.utils.NiboConstants;

import io.reactivex.Observable;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class GeocodeCordinatesUseCase extends BaseUseCase {

    IGeoCodingRepository geoCodingRepository;

    public GeocodeCordinatesUseCase(IGeoCodingRepository geoCodingRepository) {
        this.geoCodingRepository = geoCodingRepository;
    }

    @Override
    protected Observable getObservable(Params params) {
        double latitude = params.getDouble(NiboConstants.LATITUDE_PARAM, 0);
        double longitude = params.getDouble(NiboConstants.LONGITUDE_PARAM, 0);
        String apiKey = params.getString(NiboConstants.API_KEY_PARAM, null);
        return geoCodingRepository.getObservableAddressStringFromLatLng(latitude, longitude, apiKey);
    }
}
