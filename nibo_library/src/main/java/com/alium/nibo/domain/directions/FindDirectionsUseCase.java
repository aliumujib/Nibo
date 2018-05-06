package com.alium.nibo.domain.directions;

import com.alium.nibo.domain.Params;
import com.alium.nibo.domain.base.BaseUseCase;
import com.alium.nibo.repo.contracts.IDirectionsRepository;
import com.alium.nibo.utils.NiboConstants;

import io.reactivex.Observable;

/**
 * Created by aliumujib on 06/05/2018.
 */

public class FindDirectionsUseCase extends BaseUseCase {

    private IDirectionsRepository directionsRepository;

    public FindDirectionsUseCase(IDirectionsRepository directionsRepository) {
        this.directionsRepository = directionsRepository;
    }

    @Override
    protected Observable getObservable(Params params) {
        String destinationString = params.getString(NiboConstants.ORIGIN_PARAM, null);
        String originString = params.getString(NiboConstants.DESTINATION_PARAM, null);
        String apiKey = params.getString(NiboConstants.API_KEY_PARAM, null);
        return directionsRepository.getRouteForPolyline(originString, destinationString, apiKey);
    }

}
