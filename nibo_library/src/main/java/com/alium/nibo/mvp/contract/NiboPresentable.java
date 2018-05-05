/*******************************************************************************
 * Copyright (c) 2017 Francisco Gonzalez-Armijo Riádigos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.alium.nibo.mvp.contract;

import android.support.annotation.NonNull;

/**
 * Android contract for every MVP Presenter
 */
public interface NiboPresentable<V extends NiboViewable> {

    /**
     * Every NiboPresentable must implement onStart state
     */
    void onStart();

    /**
     * Every NiboPresentable must implement onViewCreated state
     */
    void onViewCreated();

    /**
     * Every NiboPresentable must implement onResume state
     */
    void onResume();


    /**
     * Every NiboPresentable must implement onPause state
     */
    void onPause();


    /**
     * Every NiboPresentable must implement onStop state
     */
    void onStop();


    /**
     * Every NiboPresentable must attach a NiboViewable
     *
     * @param viewable NiboViewable
     */
    void attachView(@NonNull V viewable);


    /**
     * Every NiboPresentable must detach its NiboViewable
     */
    void detachView();


    /**
     * Every NiboPresentable must be able to access to its attached View
     *
     * @return V NiboViewable
     */
    V getView();


    /**
     * Every NiboPresentable must know if it's view is attached
     */
    boolean isViewAttached();



    /**
     * Every NiboPresentable must know if it's view is attached
     */
    void checkViewAttached();


    /**
     * Every NiboPresentable needs an API key
     */
    void setGoogleAPIKey(String apiKey);
}
