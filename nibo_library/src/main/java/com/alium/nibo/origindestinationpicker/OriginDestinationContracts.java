package com.alium.nibo.origindestinationpicker;


import com.alium.nibo.mvp.contract.NiboPresentable;
import com.alium.nibo.mvp.contract.NiboViewable;

/**
 * Created by aliumujib on 18/04/2018.
 */

public interface OriginDestinationContracts {

    interface Presenter extends NiboPresentable<View> {


    }


    interface View extends NiboViewable<Presenter> {


    }

}
