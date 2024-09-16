package com.example.intramurospathfinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Maps extends Fragment {

    private static final String DIRECTIONS_API_KEY = "AIzaSyDxl8KLaNgRs4kuiisFIIYSJxp1wLCeRmE";
    private LatLng pointA, pointB;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {



        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */





        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng intramuros = new LatLng(14.591473, 120.975280); // Latitude and longitude of Intramuros

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(intramuros, 16)); // 15 is the zoom level




            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (pointA == null) {
                        pointA = latLng;
                        googleMap.addMarker(new MarkerOptions().position(pointA).title("Point A"));
                    } else if (pointB == null) {
                        pointB = latLng;
                        googleMap.addMarker(new MarkerOptions().position(pointB).title("Point B"));
                        drawPath(googleMap, pointA, pointB);
                    } else {
                        // Reset the points if the user clicks on the map after the path has been drawn
                        googleMap.clear();
                        pointA = latLng;
                        pointB = null;
                        googleMap.addMarker(new MarkerOptions().position(pointA).title("Point A"));
                    }
                }
            });
        }


    };


    private void drawPath(GoogleMap googleMap, LatLng origin, LatLng destination) {
        String url = buildDirectionsUrl(origin, destination);

        // Make the HTTP request in a separate thread
        new Thread(() -> {
            try {
                String jsonResponse = makeHttpRequest(url);
                List<LatLng> path = parseDirectionsResponse(jsonResponse);

                // Update the map in the main thread
                getActivity().runOnUiThread(() -> {
                    PolylineOptions polylineOptions = new PolylineOptions().addAll(path);
                    googleMap.addPolyline(polylineOptions);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        reader.close();
        connection.disconnect();

        return stringBuilder.toString();
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
    private List<LatLng> parseDirectionsResponse(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray routes = jsonObject.getJSONArray("routes");
        JSONObject route = routes.getJSONObject(0);
        JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
        String points = overviewPolyline.getString("points");

        return decodePolyline(points);

    }

    private String buildDirectionsUrl(LatLng origin, LatLng destination) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDest = "destination=" + destination.latitude + "," + destination.longitude;
        String key = "key=" + DIRECTIONS_API_KEY;
        String parameters = strOrigin + "&" + strDest + "&" + key;

        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}