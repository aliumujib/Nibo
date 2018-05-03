package com.alium.nibo.drawable;

import android.content.Context;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;

public class SupportDrawerArrowDrawable extends DrawerArrowDrawable {

    public SupportDrawerArrowDrawable(Context themedContext) {
        super(themedContext);
    }

    public void setPosition(float position) {
        if (position == 1f) {
            setVerticalMirror(true);
        } else if (position == 0f) {
            setVerticalMirror(false);
        }
        super.setProgress(position);
    }

    public float getPosition() {
        return super.getProgress();
    }
}