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

package com.alium.nibo.mvp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alium.nibo.mvp.contract.NiboPresentable;
import com.alium.nibo.mvp.contract.NiboViewable;

public abstract class BaseNiboFragment<T extends NiboPresentable> extends Fragment implements NiboViewable<T> {

    protected T presenter;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();
        if (getPresenter() != null) {
            getPresenter().onStart();
        }
    }

    public static final String ARGS_INSTANCE = "com.moehandi.instafragment";


    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        //noinspection unchecked
        injectDependencies();

        if (getPresenter() != null) {
            getPresenter().attachView(this);
        }

        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getPresenter() != null) {
            getPresenter().onViewCreated();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyView() {
        if (getPresenter() != null) {
            getPresenter().detachView();
        }
        super.onDestroyView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        if (getPresenter() != null) {
            getPresenter().onStop();
        }
        super.onStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        presenter = null;
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void displayError(String message) {
        View rootContent = getActivity().findViewById(android.R.id.content);
        Snackbar.make(rootContent, message, Snackbar.LENGTH_LONG).show();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void displayError(int messageId) {
        displayError(getString(messageId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showLoading() {
        // no-op by default
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideLoading() {
        // no-op by default
    }

    @Override
    public void injectDependencies() {

    }

    @Override
    public void attachToPresenter() {

    }

    @Override
    public void detachFromPresenter() {

    }

    @Override
    public void onLandscape() {

    }

    @Override
    public void onPortrait() {

    }

    @Override
    public void displayMessage(String message) {

    }

    @Override
    public void showNoNetwork() {

    }

    @Override
    public void close() {
        getAppCompatActivity().finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void injectPresenter(T presenter) {
        this.presenter = presenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getPresenter() {
        return presenter;
    }


    protected abstract int getLayoutId();


    public interface FragmentNavigator {
        void changeFragment(String fragmentTag,
                            @Nullable Pair<View, String> sharedElement);
    }


    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }
}
