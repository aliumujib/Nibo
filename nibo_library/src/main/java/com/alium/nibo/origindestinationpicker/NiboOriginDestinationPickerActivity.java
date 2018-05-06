package com.alium.nibo.origindestinationpicker;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;
import android.util.Log;

import com.alium.nibo.R;
import com.alium.nibo.base.BaseNiboActivity;
import com.alium.nibo.utils.NiboStyle;

public class NiboOriginDestinationPickerActivity extends BaseNiboActivity {

    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_origin_destination_picker);

        Log.wtf(TAG, "What the actual fuck?");

        replaceFragment(mConfig.build(),
                this);
    }

    private static NiboOriginDestinationPickerBuilder mConfig = new NiboOriginDestinationPickerBuilder();

    public static void setBuilder(NiboOriginDestinationPickerBuilder config) {

        if (config == null) {
            throw new NullPointerException("Config cannot be passed null. Not setting config will use default values.");
        }

        mConfig = config;
    }


    public static class NiboOriginDestinationPickerBuilder {

        private String originEditTextHint;
        private String destinationEditTextHint;
        private NiboStyle styleEnum;
        private
        @RawRes
        int styleFileID;
        private
        @DrawableRes
        int originMarkerPinIconRes;
        @DrawableRes
        int destinationMarkerPinIconRes;
        @DrawableRes
        int backButtonIconRes;
        @DrawableRes
        int textFieldClearIconRes;
        @DrawableRes
        int primaryPolyLineColor;
        @ColorRes
        int secondaryPolyLineColor;


        public NiboOriginDestinationPickerBuilder setOriginEditTextHint(String originEditTextHint) {
            this.originEditTextHint = originEditTextHint;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setDestinationEditTextHint(String destinationEditTextHint) {
            this.destinationEditTextHint = destinationEditTextHint;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setOriginMarkerPinIconRes(int originMarkerPinIconRes) {
            this.originMarkerPinIconRes = originMarkerPinIconRes;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setDestinationMarkerPinIconRes(int destinationMarkerPinIconRes) {
            this.destinationMarkerPinIconRes = destinationMarkerPinIconRes;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setBackButtonIconRes(int backButtonIconRes) {
            this.backButtonIconRes = backButtonIconRes;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setTextFieldClearIconRes(int textFieldClearIconRes) {
            this.textFieldClearIconRes = textFieldClearIconRes;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setPrimaryPolyLineColor(int primaryPolyLineColor) {
            this.primaryPolyLineColor = primaryPolyLineColor;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setSecondaryPolyLineColor(int secondaryPolyLineColor) {
            this.secondaryPolyLineColor = secondaryPolyLineColor;
            return this;
        }


        public NiboOriginDestinationPickerBuilder setStyleEnum(NiboStyle styleEnum) {
            this.styleEnum = styleEnum;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setStyleFileID(int styleFileID) {
            this.styleFileID = styleFileID;
            return this;
        }

        public NiboOriginDestinationPickerFragment build() {
            return NiboOriginDestinationPickerFragment.newInstance(originEditTextHint, destinationEditTextHint, styleEnum, styleFileID, originMarkerPinIconRes, destinationMarkerPinIconRes,
                    backButtonIconRes, textFieldClearIconRes, primaryPolyLineColor, secondaryPolyLineColor);
        }

    }


}
