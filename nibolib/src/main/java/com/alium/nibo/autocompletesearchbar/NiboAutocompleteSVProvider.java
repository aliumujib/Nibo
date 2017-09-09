package com.alium.nibo.autocompletesearchbar;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by abdulmujibaliu on 9/9/17.
 */

public interface NiboAutocompleteSVProvider {

    GoogleApiClient getGoogleApiClient();

    void onHomeButtonClicked();

    NiboPlacesAutoCompleteSearchView.SearchListener getSearchListener();

    boolean getShouldUseVoice();

}
