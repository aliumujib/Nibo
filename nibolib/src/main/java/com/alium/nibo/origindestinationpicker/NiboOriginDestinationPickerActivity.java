package com.alium.nibo.origindestinationpicker;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.alium.nibo.R;
import com.alium.nibo.origindestinationpicker.fragment.NiboOriginDestinationPickerFragment;
import com.alium.nibo.utils.NiboStyle;

public class NiboOriginDestinationPickerActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_origin_destination_picker);

        Log.wtf(TAG, "What the actual fuck?");
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
        int doneFabIconRes;
        @ColorRes
        int backButtonColorRes;
        @ColorRes
        int originCircleViewColorRes;
        @ColorRes
        int destinationCircleViewColorRes;
        @ColorRes
        int originDestinationSeperatorLineColorRes;
        @ColorRes
        int doneFabColorRes;

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

        public NiboOriginDestinationPickerBuilder setDoneFabIconRes(int doneFabIconRes) {
            this.doneFabIconRes = doneFabIconRes;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setBackButtonColorRes(int backButtonColorRes) {
            this.backButtonColorRes = backButtonColorRes;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setOriginCircleViewColorRes(int originCircleViewColorRes) {
            this.originCircleViewColorRes = originCircleViewColorRes;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setDestinationCircleViewColorRes(int destinationCircleViewColorRes) {
            this.destinationCircleViewColorRes = destinationCircleViewColorRes;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setOriginDestinationSeperatorLineColorRes(int originDestinationSeperatorLineColorRes) {
            this.originDestinationSeperatorLineColorRes = originDestinationSeperatorLineColorRes;
            return this;
        }

        public NiboOriginDestinationPickerBuilder setDoneFabColorRes(int doneFabColorRes) {
            this.doneFabColorRes = doneFabColorRes;
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
           //return NiboOriginDestinationPickerFragment.newInstance(searchBarTitle, confirmButtonTitle, styleEnum, styleFileID, markerPinIconRes);
        }

    }


}
