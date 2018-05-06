package com.alium.nibo.domain.places;

import com.alium.nibo.domain.Params;
import com.alium.nibo.domain.base.BaseUseCase;
import com.alium.nibo.repo.contracts.ISuggestionRepository;
import com.alium.nibo.utils.NiboConstants;

import io.reactivex.Observable;

/**
 * Created by aliumujib on 06/05/2018.
 */

public class GetPlaceSuggestionsUseCase extends BaseUseCase {
    private ISuggestionRepository suggestionRepository;

    public GetPlaceSuggestionsUseCase(ISuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    protected Observable getObservable(Params params) {
        return suggestionRepository.getSuggestions(params.getString(NiboConstants.SUGGESTION_QUERY_PARAM, null));
    }
}
