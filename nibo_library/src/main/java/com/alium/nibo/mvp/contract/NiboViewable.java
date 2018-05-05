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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Android contract for every MVP View in the library
 */
public interface NiboViewable<T> {

    /**
     * Every NiboViewable must be able to access to its attached Presenter
     *
     * @return NiboPresentable
     */
    T getPresenter();

    /**
     * Every NiboViewable must be able to inject its Presenter
     *
     * @param presenter NiboPresentable
     */
    void injectPresenter(T presenter);

    /**
     * Every NiboViewable must have a error message system
     */
    void displayError(String message);

    /**
     * Every NiboViewable must have a error message system
     */
    void displayError(int messageId);

    /**
     * Every NiboViewable must implement one show loading feature
     */
    void showLoading();

    /**
     * Every NiboViewable must implement one hide loading feature
     */
    void hideLoading();


    void close();


    void injectDependencies();

    void attachToPresenter();

    void detachFromPresenter();

    void onLandscape();

    void onPortrait();

    void displayMessage(String message);

    void showNoNetwork();

    Context getContext();

    void connectGoogleApiClient();

}
