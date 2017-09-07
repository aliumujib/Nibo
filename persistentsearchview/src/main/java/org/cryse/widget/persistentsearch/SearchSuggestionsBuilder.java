package org.cryse.widget.persistentsearch;

import java.util.Collection;

import io.reactivex.Observable;

public interface SearchSuggestionsBuilder {

    Collection<SearchItem> buildEmptySearchSuggestion(int maxCount);

    Observable<Collection<SearchItem>> rXbuildSearchSuggestion(int maxCount, String query);

}
