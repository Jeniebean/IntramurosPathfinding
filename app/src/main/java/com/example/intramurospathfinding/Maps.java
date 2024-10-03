package com.example.intramurospathfinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
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
    ArrayList<Map<String, Object>> ridePath = new ArrayList<>();
    Map<String, Object> selectedPath = new HashMap<>();
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

                        resetMapPath.setVisibility(View.VISIBLE);
                    } else {


                    }
                }
            });
        }


    };


    private HashMap<Polyline, List<LatLng>> polylineData = new HashMap<>();

        /**
     * Draws a path on the map between the origin and destination.
     * @param googleMap the GoogleMap object
     * @param origin the starting point of the path
     * @param destination the ending point of the path
     */
    private void drawPath(GoogleMap googleMap, LatLng origin, LatLng destination) {
        String url = buildGraphHopperUrl(origin, destination);
        int[] colors = {Color.RED, Color.BLUE, Color.GREEN};

        new Thread(() -> {
            try {
                String jsonResponse = makeHttpRequest(url);
                List<List<LatLng>> allPaths = parseGraphHopperResponse(jsonResponse);
                updateMapWithPaths(googleMap, allPaths, colors);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Updates the map with the given paths.
     * @param googleMap the GoogleMap object
     * @param allPaths the paths to be drawn on the map
     * @param colors the colors to be used for the paths
     */
    private void updateMapWithPaths(GoogleMap googleMap, List<List<LatLng>> allPaths, int[] colors) {
        getActivity().runOnUiThread(() -> {
            for (int i = 0; i < allPaths.size(); i++) {
                List<LatLng> path = allPaths.get(i);
                PolylineOptions polylineOptions = new PolylineOptions().addAll(path).color(colors[i % colors.length]).zIndex(1000).clickable(true);
                Polyline polyline = googleMap.addPolyline(polylineOptions);
                polylineData.put(polyline, path);
            }
            setPolylineClickListener(googleMap);
        });
    }

    /**
     * Sets a click listener for the polylines on the map.
     * @param googleMap the GoogleMap object
     */
    private void setPolylineClickListener(GoogleMap googleMap) {
        googleMap.setOnPolylineClickListener(polyline -> {
            List<LatLng> path = polylineData.get(polyline);
            List<List<LatLng>> pathsList = new ArrayList<>(polylineData.values());
            int selectedIndex = pathsList.indexOf(path);
            selectedPath = ridePath.get(selectedIndex);


            showStartRideDialog(path);
        });
    }

    /**
     * Shows a dialog to confirm the start of the ride.
     * @param path the path of the ride
     */
    /**
     * Shows a dialog to confirm the start of the ride.
     * @param path the path of the ride
     */
    private void showStartRideDialog(List<LatLng> path) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("Start Ride");

        // Calculate the distance and duration of the ride
        double distance = Double.parseDouble(selectedPath.get("distance").toString());
        double duration = Double.parseDouble(selectedPath.get("duration").toString());

        // Format the distance and duration for display
        String distanceStr = String.format("%.2f km", distance / 1000);
        String durationStr = String.format("%.2f min", duration / 60000);

        // Include the distance and duration in the dialog message
        String message = String.format("Are you sure you want to start the ride?\n\nDistance: %s\nEstimated Duration: %s", distanceStr, durationStr);
        builder.setMessage(message);

        LayoutInflater inflater = getLayoutInflater();
        fragment_modal  = inflater.inflate(R.layout.fragment_map_modal, null);
        setupDialogView();
        builder.setView(fragment_modal);
        setDialogButtons(builder, path);
        builder.show();
    }

    /**
     * Sets up the view for the dialog.
     */
    private void setupDialogView() {
        MaterialRadioButton regularRadioButton = fragment_modal.findViewById(R.id.regularRadioButton);
        regularRadioButton.setChecked(true);
        TextInputEditText passengerQuantityEditText = fragment_modal.findViewById(R.id.passengerQuantityEditText);
        passengerQuantityEditText.setText("1");
    }

    /**
     * Sets the positive and negative buttons for the dialog.
     * @param builder the MaterialAlertDialogBuilder object
     * @param path the path of the ride
     */
    private void setDialogButtons(MaterialAlertDialogBuilder builder, List<LatLng> path) {
        builder.setPositiveButton("Yes", (dialog, which) -> {
            storeRide(path);
            Toast.makeText(getContext(), "Ride Started", Toast.LENGTH_SHORT).show();
            clearMap();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
    }

    /**
     * Clears the map.
     */
    private void clearMap() {
        pointA = null;
        pointB = null;
        resetMapPath.setVisibility(View.INVISIBLE);
        googleMap.clear();
    }
    private String buildGraphHopperUrl(LatLng origin, LatLng destination) {
        String API_KEY = "de1f07ab-44a7-4195-80aa-ca8f105cbc91";
        String strOrigin = "point=" + origin.latitude + "," + origin.longitude;
        String strDest = "point=" + destination.latitude + "," + destination.longitude;
        String key = "&key=" + API_KEY;
        String multiplePaths = "algorithm=alternative_route&alternative_route.max_paths=3&alternative_route.max_weight_factor=20&alternative_route.max_share_factor=20&alternative_route.min_plateau_factor=0.3&alternative_route.min_factor=0.3&alternative_route.max_factor=50&alternative_route.min_paths=2&alternative_route.min_weight_factor=0.7&alternative_route.min_share_factor=0.3&alternative_route.max_plateau_factor=0.7";
        String parameters = strOrigin + "&" + strDest + "&vehicle=car&locale=en&" + multiplePaths + key;

        System.out.println("https://graphhopper.com/api/1/route?" + parameters);
        return "https://graphhopper.com/api/1/route?" + parameters;
    }



    private List<List<LatLng>> parseGraphHopperResponse(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray paths = jsonObject.getJSONArray("paths");
        List<List<LatLng>> allPaths = new ArrayList<>();

        for (int i = 0; i < paths.length(); i++) {
            JSONObject path = paths.getJSONObject(i);
            String pointsStr = path.getString("points");

            ridePath.add(new HashMap<String, Object>(){{
                put("distance", path.get("distance"));
                put("duration", path.get("time"));
            }});
            allPaths.add(decodePolyline(pointsStr));
        }

        return allPaths;
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

    Button resetMapPath;
    View fragment_modal;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        resetMapPath = (Button) v.findViewById(R.id.resetMapPath);
        resetMapPath.setVisibility(View.INVISIBLE);

        resetMapPath.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        googleMap.clear();
                        pointA = null;
                        pointB = null;
                        resetMapPath.setVisibility(View.INVISIBLE);
                    }
                }
        );


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


    public void storeRide(List<LatLng> path){
        Map<String, Object> rideDetails = new HashMap<>();
        rideDetails.put("pointA", pointA.toString());
        rideDetails.put("pointB", pointB.toString());
        rideDetails.put("status", "ongoing");
        rideDetails.put("user", CurrentUser.user_id);
        rideDetails.put("date_started", System.currentTimeMillis());
        rideDetails.put("date_ended", null);
        rideDetails.put("distance", selectedPath.get("distance"));
        rideDetails.put("duration", selectedPath.get("duration"));
        rideDetails.put("fare", 0);
        rideDetails.put("path", path);
        rideDetails.put("vehicle_type", CurrentUser.vehicle_type);

        RadioGroup radioGroup = fragment_modal.findViewById(R.id.fareTypeGroup);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.regularRadioButton) {
            rideDetails.put("fare_type", "regular");
        }
        else if (selectedId == R.id.studentRadioButton) {
            rideDetails.put("fare_type", "student");
        }
        else if (selectedId == R.id.pwdRadioButton) {
            rideDetails.put("fare_type", "pwd");

        }
        else {
            rideDetails.put("fare_type", "senior");
        }

        TextInputEditText passengerQuantityEditText = fragment_modal.findViewById(R.id.passengerQuantityEditText);


        rideDetails.put("passenger_quantity", Integer.parseInt(passengerQuantityEditText.getText().toString()));
        rideDetails.put("extension", 1);
        db.collection("rides").add(rideDetails);


    }
}