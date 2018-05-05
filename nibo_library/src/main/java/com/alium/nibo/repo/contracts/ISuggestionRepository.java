package com.alium.nibo.repo.contracts;

import com.alium.nibo.autocompletesearchbar.NiboSearchSuggestionItem;

import java.util.Collection;

import io.reactivex.Observable;

/**
 * Created by aliumujib on 05/05/2018.
 */

public interface ISuggestionRepository {

    Observable<Collection<NiboSearchSuggestionItem>> getSuggestions(final String query);

    void stop();
}
