package com.alium.nibo.repo.directions;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.alium.nibo.models.Distance;
import com.alium.nibo.models.Duration;
import com.alium.nibo.models.Route;
import com.alium.nibo.repo.contracts.DirectionFinderListener;
import com.alium.nibo.utils.NiboConstants;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abdul-Mujib Aliu on 4/3/2016.
 */

public class DirectionFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private String GOOGLE_API_KEY;
    private Activity activity;
    private DirectionFinderListener listener;
    private String origin;
    private String destination;
    private String TAG = getClass().getSimpleName();

    public DirectionFinder(String apiKey, DirectionFinderListener listener, String origin, String destination) {
        this.GOOGLE_API_KEY = apiKey;
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }


    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        Log.d(TAG, createUrl());
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");

        if (GOOGLE_API_KEY == null) {
            throw new IllegalStateException("Please provide a valid google maps DirectionsAPI key");
        }

        return DIRECTION_URL_API + "origin=" + urlOrigin + "&destination=" + urlDestination + "&key=" + GOOGLE_API_KEY;
    }

    private class DownloadRawData extends AsyncTask<String, Void, String> {

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
                listener.onDirectionFinderError(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                //Log.d(TAG, res);
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
                listener.onDirectionFinderError(e.getMessage());
            }
        }
    }

    private void parseJSon(String data) throws JSONException {
        if (data == null)
            return;

        JSONObject jsonData = new JSONObject(data);
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
                listener.onDirectionFinderSuccess(routes);
            } else if (status.equals(NiboConstants.NOT_FOUND)) {
                listener.onDirectionFinderError("Invalid request, please select another location");
            } else if (status.equals(NiboConstants.ZERO_RESULTS)) {
                listener.onDirectionFinderError("no route could be found between the selected locations");
            } else if (status.equals(NiboConstants.MAX_WAYPOINTS_EXCEEDED)) {
                listener.onDirectionFinderError("maximum waypoint limit exceeded");
            } else if (status.equals(NiboConstants.INVALID_REQUEST)) {
                listener.onDirectionFinderError("invalid request, please select two locations");
            } else if (status.equals(NiboConstants.OVER_QUERY_LIMIT)) {
                listener.onDirectionFinderError("query limit exceeded");
            } else if (status.equals(NiboConstants.REQUEST_DENIED)) {
                listener.onDirectionFinderError("unauthorized usage");
            } else if (status.equals(NiboConstants.UNKNOWN_ERROR)) {
                listener.onDirectionFinderError("unknown error");
            }

        }
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
