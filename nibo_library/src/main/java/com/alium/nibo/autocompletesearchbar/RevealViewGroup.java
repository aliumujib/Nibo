package com.alium.nibo.autocompletesearchbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.alium.nibo.autocompletesearchbar.animation.CircularRevealAnimator;
import com.alium.nibo.autocompletesearchbar.animation.BaseSupportAnimator;
import com.alium.nibo.autocompletesearchbar.animation.ViewAnimationUtilties;


public abstract class RevealViewGroup extends ViewGroup implements CircularRevealAnimator {

    private Path mRevealPath;
    private final Rect mTargetBounds = new Rect();
    private CircularRevealAnimator.RevealInfo mRevealInfo;
    private boolean mRunning;
    private float mRadius;

    public RevealViewGroup(Context context) {
        this(context, null);
    }

    public RevealViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mRevealPath = new Path();
    }

    @Override
    public void onRevealAnimationStart() {
        mRunning = true;
    }

    @Override
    public void onRevealAnimationEnd() {
        mRunning = false;
        invalidate(mTargetBounds);
    }

    @Override
    public void onRevealAnimationCancel() {
        onRevealAnimationEnd();
    }

    /**
     * Circle radius size
     *
     * @hide
     */
    @Override
    public void setRevealRadius(float radius){
        mRadius = radius;
        mRevealInfo.getTarget().getHitRect(mTargetBounds);
        invalidate(mTargetBounds);
    }

    /**
     * Circle radius size
     *
     * @hide
     */
    @Override
    public float getRevealRadius(){
        return mRadius;
    }

    /**
     * @hide
     */
    @Override
    public void attachRevealInfo(RevealInfo info) {
        mRevealInfo = info;
    }

    /**
     * @hide
     */
    @Override
    public BaseSupportAnimator startReverseAnimation() {
        if(mRevealInfo != null && mRevealInfo.hasTarget() && !mRunning) {
            return ViewAnimationUtilties.createCircularReveal(mRevealInfo.getTarget(),
                    mRevealInfo.centerX, mRevealInfo.centerY,
                    mRevealInfo.endRadius, mRevealInfo.startRadius);
        }
        return null;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if(mRunning && child == mRevealInfo.getTarget()){
            final int state = canvas.save();

            mRevealPath.reset();
            mRevealPath.addCircle(mRevealInfo.centerX, mRevealInfo.centerY, mRadius, Path.Direction.CW);

            canvas.clipPath(mRevealPath);

            boolean isInvalided = super.drawChild(canvas, child, drawingTime);

            canvas.restoreToCount(state);

            return isInvalided;
        }

        return super.drawChild(canvas, child, drawingTime);
    }

}