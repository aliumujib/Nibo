package com.alium.nibo;

/**
 * Created by abdulmujibaliu on 9/7/17.
 */

public enum NiboStyle {

    BLUE_ESSENCE(R.raw.blue_essense), GREY_UBER(R.raw.grey_uber), HOPPER(R.raw.hopper), NIGHT_MODE(R.raw.night_mode), RETRO(R.raw.retro), UNSATURATED_BROWNS(R.raw.unsaturated_browns), DEFAULT(0), CUSTOM(-1);

    private final int value;

    private NiboStyle(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
