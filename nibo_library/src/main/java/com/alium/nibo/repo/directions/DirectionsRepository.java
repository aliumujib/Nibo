package com.alium.nibo.repo.directions;

import com.alium.nibo.models.Distance;
import com.alium.nibo.models.Duration;
import com.alium.nibo.models.Route;
import com.alium.nibo.repo.api.DirectionsAPI;
import com.alium.nibo.repo.contracts.IDirectionsRepository;
import com.alium.nibo.utils.NiboConstants;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by aliumujib on 03/05/2018.
 */

public class DirectionsRepository implements IDirectionsRepository {

    private final DirectionsAPI directionsApi;

    public DirectionsRepository(DirectionsAPI directionsApi) {
        this.directionsApi = directionsApi;
    }

    @Override
    public Observable<List<Route>> getRouteForPolyline(final String origin, final String destination, final String apiKey) {
        return Observable.create(new ObservableOnSubscribe<List<Route>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<Route>> source) throws Exception {
                directionsApi.getPolylineData(String.valueOf(origin), String.valueOf(destination), apiKey).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        JsonObject jsonObject = response.body();
                        try {
                            //I had an old implementation where I parsed this with org.json.JSONObject ... I don't want to rewrite it .. sooooo

                            JSONObject jsonData = new JSONObject(jsonObject.toString());
                            List<Route> routes = new ArrayList<Route>();

                            if (jsonData.has("status")) {
                                String status = jsonData.getString("status");
                                if (status.equals(NiboConstants.OK)) {
                                    JSONArray jsonRoutes = jsonData.getJSONArray("routes");
                                    for (int i = 0; i < jsonRoutes.length(); i++) {
                                        JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                                        Route route = new Route();

                                        JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                                        JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                                        JSONObject jsonLeg = jsonLegs.getJSONObject(0);
                                        JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                                        JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                                        JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
                                        JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

                                        route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
                                        route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
                                        route.endAddress = jsonLeg.getString("end_address");
                                        route.startAddress = jsonLeg.getString("start_address");
                                        route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
                                        route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
                                        route.points = decodePolyLine(overview_polylineJson.getString("points"));

                                        routes.add(route);
                                    }

                                    /**
                                     * https://developers.google.com/maps/documentation/javascript/directions#DirectionsStatus
                                     *
                                     * OK indicates the response contains a valid DirectionsResult.
                                     * NOT_FOUND indicates at least one of the locations specified in the request's origin, destination, or waypoints could not be geocoded.
                                     * ZERO_RESULTS indicates no route could be found between the origin and destination.
                                     * MAX_WAYPOINTS_EXCEEDED indicates that too many DirectionsWaypoint fields were provided in the DirectionsRequest. See the section below on limits for way points.
                                     * INVALID_REQUEST indicates that the provided DirectionsRequest was invalid. The most common causes of this error code are requests that are missing either an origin or destination, or a transit request that includes waypoints.
                                     * OVER_QUERY_LIMIT indicates the webpage has sent too many requests within the allowed time period.
                                     * REQUEST_DENIED indicates the webpage is not allowed to use the directions service.
                                     * UNKNOWN_ERROR indicates a directions request could not be processed due to a server error. The request may succeed if yo
                                     */
                                    source.onNext(routes);
                                } else if (status.equals(NiboConstants.NOT_FOUND)) {
                                    source.onError(new Throwable("Invalid request, please select another location"));
                                } else if (status.equals(NiboConstants.ZERO_RESULTS)) {
                                    source.onError(new Throwable("no route could be found between the selected locations"));
                                } else if (status.equals(NiboConstants.MAX_WAYPOINTS_EXCEEDED)) {
                                    source.onError(new Throwable("maximum waypoint limit exceeded"));
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

                        } catch (Exception e) {
                            source.onError(e);
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


    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
