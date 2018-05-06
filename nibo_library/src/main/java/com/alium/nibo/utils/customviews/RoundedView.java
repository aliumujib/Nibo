package com.alium.nibo.utils.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.alium.nibo.R;

/**
 * Created by abdulmujibaliu on 9/7/17.
 */

public class RoundedView extends View {


    public RoundedView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RoundedView, 0, 0);

        uncheckedColor = a.getColor(R.styleable.RoundedView_colorUnselected, ContextCompat.getColor(getContext(), R.color.circle_color_default_gray));
        checkedColor = a.getColor(R.styleable.RoundedView_colorSelected, ContextCompat.getColor(getContext(), R.color.black));

        paintColor = uncheckedColor;

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RoundedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    Paint paint = new Paint(Paint.DITHER_FLAG);

    @Override
    protected void onDraw(Canvas canvas) {

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);

        paint.setColor(paintColor);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2,
                getWidth() / 2, paint);

    }

    private int uncheckedColor;
    private int checkedColor;

    private int paintColor = uncheckedColor;
    private boolean checked = false;

    public void setCheckedCircleColor(int color) {
        this.checkedColor = color;
        invalidate();
    }

    public void setUncheckedCircleColor(int color) {
        this.uncheckedColor = color;
        invalidate();
    }


    public void setChecked(boolean checked) {
        this.checked = checked;
        if (this.checked) {
            paintColor = checkedColor;
        } else {
            paintColor = uncheckedColor;
        }
        invalidate();
    }

    public boolean isChecked() {
        return checked;
    }


}