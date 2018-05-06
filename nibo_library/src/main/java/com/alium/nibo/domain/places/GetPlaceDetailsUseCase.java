package com.alium.nibo.domain.places;

import com.alium.nibo.domain.Params;
import com.alium.nibo.domain.base.BaseUseCase;
import com.alium.nibo.repo.contracts.ILocationRepository;
import com.alium.nibo.repo.contracts.ISuggestionRepository;
import com.alium.nibo.utils.NiboConstants;

import io.reactivex.Observable;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class GetPlaceDetailsUseCase extends BaseUseCase {

    ISuggestionRepository suggestionRepository;

    public GetPlaceDetailsUseCase(ISuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    protected Observable getObservable(Params params) {
        String placeId = params.getString(NiboConstants.PLACE_ID_PARAM, null);
        return suggestionRepository.getPlaceByID(placeId);
    }

}
