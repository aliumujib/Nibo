package com.alium.niboexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alium.nibo.origindestinationpicker.NiboOriginDestinationPickerActivity;
import com.alium.nibo.utils.NiboConstants;
import com.alium.nibo.placepicker.NiboPlacePickerActivity;
import com.alium.nibo.utils.NiboStyle;
import com.alium.nibo.models.NiboSelectedPlace;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchStartFinishActivity();
            }
        });
    }


    private void launchPickerFragment() {
        Intent intent = new Intent(this, NiboPlacePickerActivity.class);
        NiboPlacePickerActivity.NiboPlacePickerBuilder config = new NiboPlacePickerActivity.NiboPlacePickerBuilder()
                .setSearchBarTitle("Search for an area")
                .setConfirmButtonTitle("Pick here bish")
                .setMarkerPinIconRes(R.drawable.ic_map_marker_black_36dp)
                .setStyleEnum(NiboStyle.CUSTOM)
                .setStyleFileID(R.raw.retro);
        NiboPlacePickerActivity.setBuilder(config);
        startActivityForResult(intent, 200);
    }


    private void launchStartFinishActivity() {
        Intent intent = new Intent(this, NiboOriginDestinationPickerActivity.class);

        NiboOriginDestinationPickerActivity.NiboOriginDestinationPickerBuilder config = new NiboOriginDestinationPickerActivity.NiboOriginDestinationPickerBuilder()
                .setDestinationMarkerPinIconRes(R.drawable.ic_map_marker_black_36dp)
                .setOriginMarkerPinIconRes(R.drawable.ic_map_marker_black_36dp)
                .setOriginEditTextHint("Input pick up location")
                .setDestinationCircleViewColorRes(R.color.colorAccent)
                .setTextFieldClearIconRes(R.drawable.ic_close_black_18dp)
                .setOriginCircleViewColorRes(R.color.colorPrimaryDark)
                .setDoneFabIconRes(R.drawable.ic_close_white_36dp)
                .setBackButtonIconRes(R.drawable.ic_close_black_24dp)
                .setOriginDestinationSeperatorLineColorRes(R.color.colorPrimary)
                .setDestinationEditTextHint("Input destination")
                .setStyleEnum(NiboStyle.SUBTLE_GREY_SCALE);

        NiboOriginDestinationPickerActivity.setBuilder(config);
        startActivityForResult(intent, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 200) {
            NiboSelectedPlace selectedPlace = data.getParcelableExtra(NiboConstants.RESULTS_SELECTED);
            Toast.makeText(this, selectedPlace.getPlaceAddress(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error getting images", Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
