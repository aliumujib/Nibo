package com.alium.nibo.placepicker;

import android.content.Context;

import com.alium.nibo.autocompletesearchbar.NiboSearchSuggestionItem;
import com.alium.nibo.autocompletesearchbar.SearchSuggestionsBuilder;
import com.alium.nibo.repo.location.SuggestionsProvider;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.Observable;

public class PlaceSuggestionsBuilder implements SearchSuggestionsBuilder {
    private Context mContext;
    private List<NiboSearchSuggestionItem> mHistorySuggestions = new ArrayList<>();

    private String TAG = getClass().getSimpleName();

    public PlaceSuggestionsBuilder(Context context) {
        this.mContext = context;
    }

    @Override
    public Collection<NiboSearchSuggestionItem> buildEmptySearchSuggestion(int maxCount) {
        List<NiboSearchSuggestionItem> items = new ArrayList<>();
        items.addAll(mHistorySuggestions);
        return items;
    }


    @Override
    public Observable<Collection<NiboSearchSuggestionItem>> rXbuildSearchSuggestion(int maxCount, final String query) {
        return SuggestionsProvider.sharedInstance.getSuggestions(query);
    }
}
