package com.alium.nibo.autocompletesearchbar;

public class BaseNiboSearchListener implements NiboPlacesAutoCompleteSearchView.SearchListener {

    @Override
    public boolean onSuggestion(NiboSearchSuggestionItem niboSearchSuggestionItem) {
        return true;
    }

    @Override
    public void onSearchCleared() {

    }

    @Override
    public void onSearchTermChanged(String term) {

    }

    @Override
    public void onSearch(String query) {

    }

    @Override
    public void onSearchEditOpened() {

    }

    @Override
    public void onSearchEditClosed() {

    }

    @Override
    public boolean onSearchEditBackPressed() {
        return false;
    }

    @Override
    public void onSearchExit() {

    }
}
