package com.alium.nibo.repo.contracts;

import android.location.Location;

import io.reactivex.subjects.PublishSubject;

/**
 * Created by abdulmujibaliu on 9/3/17.
 */

public interface ILocationRepository {

    PublishSubject<Location> getLocationObservable();



}
