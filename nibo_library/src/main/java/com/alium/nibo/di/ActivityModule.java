package com.alium.nibo.di;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by aliumujib on 05/05/2018.
 */

public class ActivityModule {

    private ActivityModule activityModule;
    private AppCompatActivity appCompatActivity;

    public ActivityModule(AppCompatActivity appCompatActivity) {
        this.appCompatActivity = appCompatActivity;
    }

    public AppCompatActivity getAppCompatActivity() {
        return appCompatActivity;
    }
    
}
