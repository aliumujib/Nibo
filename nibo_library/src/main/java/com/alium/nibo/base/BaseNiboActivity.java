package com.alium.nibo.base;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.alium.nibo.R;
import com.alium.nibo.utils.NiboConstants;

/**
 * Created by abdulmujibaliu on 9/7/17.
 */

public class BaseNiboActivity extends AppCompatActivity {


    public void replaceFragment(Fragment fragment, Context context) {
        FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();

        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.frame_container, fragment, NiboConstants._FRAGMENT_TAG);
        ft.commit();
    }



}
