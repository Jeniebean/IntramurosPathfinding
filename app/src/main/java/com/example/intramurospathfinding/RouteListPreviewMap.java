package com.example.intramurospathfinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.graphics.Color;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteListPreviewMap extends Fragment {
    Bundle bundle;
    View fragment_modal;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LatLng pointA, pointB;
    String path;
    ArrayList<LatLng> points;
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
            bundle = getArguments();

            LatLng origin = new LatLng(bundle.getDouble("originLatitude"), bundle.getDouble("originLongitude"));
            pointA = origin;
            LatLng destination = new LatLng(bundle.getDouble("destinationLatitude"), bundle.getDouble("destinationLongitude"));
            pointB = destination;
            googleMap.addMarker(new MarkerOptions().position(origin).title("Origin"));
            googleMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
            // move to marker and zoom 15
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 16));

            path = bundle.getString("path");
            String[] latLngStrings = path.split(", lat/lng: "); // Split the string into individual LatLng strings

            System.out.println("Path: " + path);
            points = new ArrayList<>();
            for (String latLngString : latLngStrings) {
                int start = latLngString.indexOf("(") + 1;
                int end = latLngString.indexOf(")");

                String[] latLngValues = latLngString.substring(start, end).split(",");
                double latitude = Double.parseDouble(latLngValues[0]);
                double longitude = Double.parseDouble(latLngValues[1]);

                LatLng latLng = new LatLng(latitude, longitude);
                points.add(latLng);
            }

            for (int i = 0; i < points.size() - 1; i++) {
                LatLng pointA = points.get(i);
                LatLng pointB = points.get(i + 1);
                googleMap.addPolyline(new PolylineOptions().add(pointA, pointB).width(5).color(Color.RED));
            }

        }
    };



    public void storeRide(List<LatLng> path){
        Map<String, Object> rideDetails = new HashMap<>();
        rideDetails.put("pointA", pointA.toString());
        rideDetails.put("pointB", pointB.toString());
        rideDetails.put("status", "ongoing");
        rideDetails.put("user", CurrentUser.user_id);
        rideDetails.put("date_started", System.currentTimeMillis());
        rideDetails.put("date_ended", null);
        rideDetails.put("distance", bundle.get("distance"));
        rideDetails.put("duration", bundle.get("duration"));
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
            // Go to HistoryFragment
            Fragment fragment = new History();
            FragmentManager fragmentManager = ((MainActivity) getContext()).getSupportFragmentManager();
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, fragment).commit();
            // Select the History tab
            ((MainActivity) getContext()).bottomNavigationView.setSelectedItemId(R.id.history);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
    }






    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_route_list_preview_map, container, false);

        Button startRideBtn = v.findViewById(R.id.startRideBtn);
        startRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("Start Ride");
                builder.setMessage("Are you sure you want to start this ride?");
                fragment_modal = inflater.inflate(R.layout.fragment_map_modal, null);
                builder.setView(fragment_modal);
                setupDialogView();
                setDialogButtons(builder, points);
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
}