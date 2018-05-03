package com.alium.nibo.repo.location;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.alium.nibo.repo.directions.DirectionFinder;
import com.alium.nibo.utils.NiboConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LocationAddress {

    private static final String TAG = "LocationAddress";

    private static final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json?";
    private String GOOGLE_API_KEY;

    public static final LocationAddress sharedLocationAddressInstance = new LocationAddress();
    Address address;

    public Observable<Address> getObservableAddressFromLocation(final double latitude, final double longitude,
                                                                final Context context) {
        return Observable.create(new ObservableOnSubscribe<Address>() {
            @Override
            public void subscribe(ObservableEmitter<Address> source) throws Exception {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    Log.d(TAG, String.valueOf(addressList.size()));
                    if (!addressList.isEmpty()) {
                        address = addressList.get(0);
                        source.onNext(address);
                    } else {
                        source.onError(new Throwable("Error getting address"));
                    }
                } catch (IOException e) {

                    source.onError(new Throwable("Error getting address"));
                    Log.e(TAG, "Unable connect to Geocoder", e);
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<String> getObservableAddressStringFromLatLng(final double latitude, final double longitude,
                                                                   final Context context) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> source) throws Exception {
                //THIS IS BAD
                //TODO REPLACE THIS SHIT WITH SOME RETROFIT AND RXJAVA LOVE
                class DownloadRawData extends AsyncTask<String, Void, String> {

                    @Override
                    protected String doInBackground(String... params) {
                        String link = params[0];
                        try {
                            URL url = new URL(link);
                            InputStream is = url.openConnection().getInputStream();
                            StringBuffer buffer = new StringBuffer();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                buffer.append(line + "\n");
                            }

                            return buffer.toString();

                        } catch (IOException e) {
                            e.printStackTrace();
                            source.onError(e);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String res) {
                        try {
                            //Log.d(TAG, res);
                            if (res != null) {
                                JSONObject jsonData = new JSONObject(res);
                                if (jsonData.has("status")) {

                                    String status = jsonData.getString("status");

                                    if (status.equals(NiboConstants.OK)) {
                                        JSONArray jsonArray = jsonData.getJSONArray("results");
                                        if (jsonArray.length() == 0) {
                                            source.onError(new Throwable("No results"));
                                        } else {
                                            JSONObject object = (JSONObject) jsonArray.get(0);
                                            String address = object.getString("formatted_address");
                                            source.onNext(address);
                                        }
                                    } else {
                                        if (!source.isDisposed()) {
                                            if (status.equals(NiboConstants.NOT_FOUND)) {
                                                source.onError(new Throwable("Invalid request, please select another location"));
                                            } else if (status.equals(NiboConstants.INVALID_REQUEST)) {
                                                source.onError(new Throwable("invalid request, please select two locations"));
                                            } else if (status.equals(NiboConstants.OVER_QUERY_LIMIT)) {
                                                source.onError(new Throwable("query limit exceeded"));
                                            } else if (status.equals(NiboConstants.REQUEST_DENIED)) {
                                                source.onError(new Throwable("unauthorized usage"));
                                            } else if (status.equals(NiboConstants.UNKNOWN_ERROR)) {
                                                source.onError(new Throwable("unknown error"));
                                            }
                                        }
                                    }
                                } else {

                                }
                            } else {
                                if (!source.isDisposed()) {
                                    source.onError(new Throwable("Error getting address"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (!source.isDisposed()) {
                                source.onError(e);
                            }
                        }
                    }
                }

                try {
                    ApplicationInfo ai = context.getPackageManager()
                            .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                    Bundle bundle = ai.metaData;
                    GOOGLE_API_KEY = bundle.getString("com.google.android.geo.API_KEY");
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
                    source.onError(e);
                } catch (NullPointerException e) {
                    Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
                    source.onError(e);
                }

                String url = GEOCODING_API_URL + "latlng=" + latitude + "," + longitude + "&key=" + GOOGLE_API_KEY;
                new DownloadRawData().execute(url);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Address getAddressFromLocation(final double latitude, final double longitude,
                                          final Context context) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> addressList = geocoder.getFromLocation(
                    latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                address = addressList.get(0);

            }
        } catch (IOException e) {
            Log.e(TAG, "Unable connect to Geocoder", e);
        }
        return address;


    }


}

