package com.alium.nibo.placepicker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.alium.nibo.R;
import com.alium.nibo.repo.location.SuggestionsRepository;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;

import org.cryse.widget.persistentsearch.SearchItem;
import org.cryse.widget.persistentsearch.SearchSuggestionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;

public class PlaceSuggestionsBuilder implements SearchSuggestionsBuilder {
    private Context mContext;
    private List<SearchItem> mHistorySuggestions = new ArrayList<SearchItem>();

    private String TAG = getClass().getSimpleName();

    public PlaceSuggestionsBuilder(Context context) {
        this.mContext = context;
    }





    @Override
    public Collection<SearchItem> buildEmptySearchSuggestion(int maxCount) {
        List<SearchItem> items = new ArrayList<SearchItem>();
        items.addAll(mHistorySuggestions);
        return items;
    }


    @Override
    public Observable<Collection<SearchItem>> rXbuildSearchSuggestion(int maxCount, final String query) {

        return SuggestionsRepository.sharedInstance.getSuggestions(query);
    }
}
