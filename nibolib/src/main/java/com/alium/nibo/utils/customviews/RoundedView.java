package com.alium.nibo.utils.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.alium.nibo.R;

import java.lang.reflect.Field;

/**
 * Created by abdulmujibaliu on 9/7/17.
 */

public class RoundedView extends View {

    public RoundedView(Context context) {
        super(context);
    }

    public RoundedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        //if (getBackgroundColor(this) != 0) color = getBackgroundColor(this);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);

        paint.setColor(color);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2,
                getWidth() / 2, paint);

//        if(text != null && !checked)
//            drawText(canvas);
//        if(checked && text == null)
//            drawChecked(canvas);
    }

    private int color = ContextCompat.getColor(getContext(), R.color.circle_color_default_gray);

    private String text = null;
    private boolean checked = false;

    public void setCircleColor(int color) {
        this.color = color;
        invalidate();
    }

    public void setCircleAccentColor() {
        color = ContextCompat.getColor(getContext(), R.color.black);
        invalidate();
    }

    public void setCircleGrayColor() {
        color = ContextCompat.getColor(getContext(), R.color.circle_color_default_gray);
        invalidate();
    }

    public void setText(String text) {
        this.text = text;
        this.checked = false;
        invalidate();
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if (this.checked) {
            setCircleColor(ContextCompat.getColor(getContext(), R.color.black));
        } else {
            setCircleGrayColor();
        }
        text = null;
        invalidate();
    }

    public boolean isChecked() {
        return checked;
    }

    private int getBackgroundColor(View view) {
        ColorDrawable drawable = (ColorDrawable) view.getBackground();
        if (drawable != null) {
            if (Build.VERSION.SDK_INT >= 11) {
                return drawable.getColor();
            }
            try {
                Field field = drawable.getClass().getDeclaredField("mState");
                field.setAccessible(true);
                Object object = field.get(drawable);
                field = object.getClass().getDeclaredField("mUseColor");
                field.setAccessible(true);
                return field.getInt(object);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return 0;
    }


}