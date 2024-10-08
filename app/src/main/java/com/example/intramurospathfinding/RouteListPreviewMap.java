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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteListPreviewMap extends Fragment {
    Bundle bundle;
    View fragment_modal;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LatLng pointA, pointB;
    String path;
    String rideTitle;
    ArrayList<LatLng> points;
    List<LatLng> wallPoints = Arrays.asList(
            new LatLng(14.5914852, 120.9718346),
            new LatLng(14.5915014, 120.9718507),
            new LatLng(14.5914102, 120.9719205),
            new LatLng(14.5913398, 120.9719614),
            new LatLng(14.5913151, 120.9719286),
            new LatLng(14.5909806, 120.9721463),
            new LatLng(14.5909535, 120.9721626),
            new LatLng(14.5899076, 120.9727895),
            new LatLng(14.5897004, 120.9726477),
            new LatLng(14.5895774, 120.9726763),
            new LatLng(14.5893044, 120.9727397),
            new LatLng(14.5891995, 120.9728619),
            new LatLng(14.5891026, 120.9729774),
            new LatLng(14.5891609, 120.9732701)

    );
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

            rideTitle = bundle.getString("title");

            LatLng origin = new LatLng(bundle.getDouble("originLatitude"), bundle.getDouble("originLongitude"));
            pointA = origin;
            LatLng destination = new LatLng(bundle.getDouble("destinationLatitude"), bundle.getDouble("destinationLongitude"));
            pointB = destination;
            googleMap.addMarker(new MarkerOptions().position(origin).title("Origin"));
            googleMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
            // move to marker and zoom 15
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 17));

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
                int color = isOnWall(pointA) || isOnWall(pointB) ? Color.BLUE : Color.RED;
                googleMap.addPolyline(new PolylineOptions().add(pointA, pointB).width(5).color(color));
            }

        }
    };


    // Check if a given LatLng point is on the wall
    private boolean isOnWall(LatLng point) {
        final double TOLERANCE = 0.0001; // Adjust this value based on how close to the wall a point needs to be to be considered "on" the wall
        for (LatLng wallPoint : wallPoints) {
            double distance = Math.sqrt(Math.pow(wallPoint.latitude - point.latitude, 2) + Math.pow(wallPoint.longitude - point.longitude, 2));
            if (distance < TOLERANCE) {
                return true;
            }
        }
        return false;
    }


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

        TextInputEditText regularPassengerQuantityEditText = fragment_modal.findViewById(R.id.regularPassengerQuantityEditText);
        TextInputEditText studentPassengerQuantityEditText = fragment_modal.findViewById(R.id.studentPassengerQuantityEditText);
        TextInputEditText seniorPassengerQuantityEditText = fragment_modal.findViewById(R.id.seniorPassengerQuantityEditText);
        TextInputEditText pwdPassengerQuantityEditText = fragment_modal.findViewById(R.id.pwdPassengerQuantityEditText);


        rideDetails.put("regular_passenger_quantity", Integer.parseInt(regularPassengerQuantityEditText.getText().toString()));
        rideDetails.put("student_passenger_quantity", Integer.parseInt(studentPassengerQuantityEditText.getText().toString()));
        rideDetails.put("senior_passenger_quantity", Integer.parseInt(seniorPassengerQuantityEditText.getText().toString()));
        rideDetails.put("pwd_passenger_quantity", Integer.parseInt(pwdPassengerQuantityEditText.getText().toString()));
        rideDetails.put("extension", 0);
        rideDetails.put("ride_title", rideTitle);
        db.collection("rides").add(rideDetails);


    }





    /**
     * Sets up the view for the dialog.
     */
    private void setupDialogView() {
        TextInputEditText regularPassengerQuantityEditText = fragment_modal.findViewById(R.id.regularPassengerQuantityEditText);
        TextInputEditText studentPassengerQuantityEditText = fragment_modal.findViewById(R.id.studentPassengerQuantityEditText);
        TextInputEditText seniorPassengerQuantityEditText = fragment_modal.findViewById(R.id.seniorPassengerQuantityEditText);
        TextInputEditText pwdPassengerQuantityEditText = fragment_modal.findViewById(R.id.pwdPassengerQuantityEditText);

        regularPassengerQuantityEditText.setText("0");
        studentPassengerQuantityEditText.setText("0");
        seniorPassengerQuantityEditText.setText("0");
        pwdPassengerQuantityEditText.setText("0");


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