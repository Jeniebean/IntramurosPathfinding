package com.example.intramurospathfinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Maps extends Fragment {

    private static final String DIRECTIONS_API_KEY = "AIzaSyDxl8KLaNgRs4kuiisFIIYSJxp1wLCeRmE";
    private LatLng pointA, pointB;
    Button startRideBtn;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> ridePath = new HashMap<>();
    GoogleMap googleMap;
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
            Maps.this.googleMap = googleMap;
            LatLng intramuros = new LatLng(14.591473, 120.975280); // Latitude and longitude of Intramuros
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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

                        startRideBtn.setVisibility(View.VISIBLE);

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
    String url = buildGraphHopperUrl(origin, destination);

    // Make the HTTP request in a separate thread
    new Thread(() -> {
        try {
            String jsonResponse = makeHttpRequest(url);
            List<LatLng> path = parseGraphHopperResponse(jsonResponse);

            // Update the map in the main thread
            getActivity().runOnUiThread(() -> {
                PolylineOptions polylineOptions = new PolylineOptions().addAll(path).zIndex(1000);
                ridePath.put("path", path);
                googleMap.addPolyline(polylineOptions);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}

    private String buildGraphHopperUrl(LatLng origin, LatLng destination) {
        String API_KEY = "de1f07ab-44a7-4195-80aa-ca8f105cbc91";
        String strOrigin = "point=" + origin.latitude + "," + origin.longitude;
        String strDest = "point=" + destination.latitude + "," + destination.longitude;
        String key = "key=" + API_KEY;
        String parameters = strOrigin + "&" + strDest + "&vehicle=foot&locale=en&" + key;

        return "https://graphhopper.com/api/1/route?" + parameters;
    }

   private List<LatLng> parseGraphHopperResponse(String jsonResponse) throws JSONException {
    JSONObject jsonObject = new JSONObject(jsonResponse);
    JSONArray paths = jsonObject.getJSONArray("paths");
    JSONObject path = paths.getJSONObject(0);
    String pointsStr = path.getString("points");
    ridePath.put("distance", path.getDouble("distance"));
    ridePath.put("duration", path.getDouble("time"));




    return decodePolyline(pointsStr);
}

    private String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        System.out.println("Successfully Connected to GraphHopper API");
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



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        startRideBtn = (Button) v.findViewById(R.id.startRideBtn);
        startRideBtn.setVisibility(View.INVISIBLE);


        startRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("Start Ride");
                builder.setMessage("Are you sure you want to start the ride?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    storeRide();
                    Toast.makeText(getContext(), "Ride Started", Toast.LENGTH_SHORT).show();
                    // Clear the map
                    pointA = null;
                    pointB = null;
                    startRideBtn.setVisibility(View.INVISIBLE);
                    googleMap.clear();

                });
                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();


            }
        });
        return v;
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


    public void storeRide(){
        Map<String, Object> rideDetails = new HashMap<>();
        rideDetails.put("pointA", pointA.toString());
        rideDetails.put("pointB", pointB.toString());
        rideDetails.put("status", "ongoing");
        rideDetails.put("user", CurrentUser.user_id);
        rideDetails.put("date_started", System.currentTimeMillis());
        rideDetails.put("date_ended", null);
        rideDetails.put("distance", ridePath.get("distance"));
        rideDetails.put("duration", ridePath.get("duration"));
        rideDetails.put("fare", 0);
        rideDetails.put("path", ridePath.get("path"));

        db.collection("rides").add(rideDetails);


    }
}