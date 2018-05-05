package com.alium.nibo.repo.location;

import android.util.Log;

import com.alium.nibo.repo.api.GeocodeAPI;
import com.alium.nibo.repo.contracts.IGeoCodingRepository;
import com.alium.nibo.utils.NiboConstants;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aliumujib on 03/05/2018.
 */

public class GeoCodingRepository implements IGeoCodingRepository {

    GeocodeAPI geocodeAPI;
    private String TAG = getClass().getSimpleName();

    public GeoCodingRepository(GeocodeAPI geocodeAPI) {
        this.geocodeAPI = geocodeAPI;
    }

    @Override
    public Observable<String> getObservableAddressStringFromLatLng(final double latitude, final double longitude, final String apiKey) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> source) throws Exception {
                String latLng = latitude + "," + longitude;
                geocodeAPI.getPolylineData(latLng, apiKey).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        try {
                            if (response.body() != null) {
                                //Log.d(TAG, res);
                                JSONObject jsonData = new JSONObject(response.body().toString());
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
                                    source.onError(new Throwable("Invalid JSON response"));
                                }
                            }else {
                                Log.d(TAG, ""+response.body());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (!source.isDisposed()) {
                                source.onError(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        source.onError(t);
                    }
                });
            }
        });
    }
}
