package com.alium.nibo.utils;

import com.alium.nibo.R;

/**
 * Created by abdulmujibaliu on 9/7/17.
 */

public enum NiboStyle {

    BLUE_ESSENCE(R.raw.blue_essense), SUBTLE_GREY_SCALE(R.raw.subtle_grey_scale), HOPPER(R.raw.hopper), NIGHT_MODE(R.raw.night_mode), RETRO(R.raw.retro), UNSATURATED_BROWNS(R.raw.unsaturated_browns), DEFAULT(0), CUSTOM(-1);

    private final int value;

    private NiboStyle(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
