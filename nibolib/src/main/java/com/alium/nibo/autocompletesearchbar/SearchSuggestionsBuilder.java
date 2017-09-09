package com.alium.nibo.autocompletesearchbar;

import java.util.Collection;

import io.reactivex.Observable;

public interface SearchSuggestionsBuilder {

    Collection<NiboSearchSuggestionItem> buildEmptySearchSuggestion(int maxCount);

    Observable<Collection<NiboSearchSuggestionItem>> rXbuildSearchSuggestion(int maxCount, String query);

}
