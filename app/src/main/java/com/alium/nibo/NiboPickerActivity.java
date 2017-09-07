package com.alium.nibo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.RawRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class NiboPickerActivity extends AppCompatActivity {


    private static NiboPickerBuilder mConfig;

    public static void setBuilder(NiboPickerBuilder config) {

        if (config == null) {
            throw new NullPointerException("Config cannot be passed null. Not setting config will use default values.");
        }

        mConfig = config;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nibo_picker);

//        new NiboPickerBuilder()
//                .setSearchBarTitle("Search for an area")
//                .setConfirmButtonTitle("Picker here bish")
//                .setMarkerPinIconRes(R.drawable.ic_place)
//                .setStyleEnum(NiboStyle.CUSTOM)
//                .setStyleFileID(R.raw.retro)
//                .build()

        replaceFragmentWithorWithoutBackState(mConfig.build(),
                this);

    }


    public static void replaceFragmentWithorWithoutBackState(Fragment fragment, Context context) {
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();

        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.frame_container, fragment, Constants._FRAGMENT_TAG);
        ft.commit();
    }

    public class NiboPickerBuilder {
        private String searchBarTitle;
        private String confirmButtonTitle;
        private NiboStyle styleEnum;
        private
        @RawRes
        int styleFileID;
        private
        @DrawableRes
        int markerPinIconRes;


        public NiboPickerBuilder setSearchBarTitle(String searchBarTitle) {
            this.searchBarTitle = searchBarTitle;
            return this;
        }

        public NiboPickerBuilder setConfirmButtonTitle(String confirmButtonTitle) {
            this.confirmButtonTitle = confirmButtonTitle;
            return this;
        }

        public NiboPickerBuilder setStyleEnum(NiboStyle styleEnum) {
            this.styleEnum = styleEnum;
            return this;
        }

        public NiboPickerBuilder setStyleFileID(int styleFileID) {
            this.styleFileID = styleFileID;
            return this;
        }

        public NiboPickerBuilder setMarkerPinIconRes(int markerPinIconRes) {
            this.markerPinIconRes = markerPinIconRes;
            return this;
        }


        public NiboPickerFragment build() {
            return NiboPickerFragment.newInstance(searchBarTitle, confirmButtonTitle, styleEnum, styleFileID, markerPinIconRes);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds mPlaceSuggestionItems to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
