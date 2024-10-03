package com.example.intramurospathfinding;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> historyList; // replace String with your actual data type
    private LayoutInflater inflater;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button extendRideBtn, endRideBtn, viewRideBtn;

    public HistoryAdapter(Context context, List<Map<String,Object>> historyList) {
        this.context = context;
        this.historyList = historyList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return historyList.size();
    }

    @Override
    public Object getItem(int position) {
        return historyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void refreshList(List<Map<String, Object>> newHistoryList) {
        this.historyList = newHistoryList;
        notifyDataSetChanged();
        int count = 0;
    }

    public void getUpdatedHistory() {
        // Fetch the updated list of rides from the database
        // This is similar to the getHistory method in your History class
        // Once you have the updated list, call refreshList
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<Map<String, Object>> updatedHistoryList = new ArrayList<>();
        db.collection("rides")
                .whereEqualTo("user", CurrentUser.user_id)
                .orderBy("date_started", Query.Direction.DESCENDING)

                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Map<String, Object> ride = new HashMap<>();
                        ride.put("start", documentSnapshot.get("start"));
                        ride.put("end", documentSnapshot.get("end"));
                        ride.put("status", documentSnapshot.get("status"));
                        ride.put("date_started", documentSnapshot.get("date_started"));
                        ride.put("date_ended", documentSnapshot.get("date_ended"));
                        ride.put("duration", documentSnapshot.get("duration"));
                        ride.put("distance", documentSnapshot.get("distance"));
                        ride.put("fare", documentSnapshot.get("fare"));
                        ride.put("ride_id", documentSnapshot.getId());

                        updatedHistoryList.add(ride);
                    }
                    refreshList(updatedHistoryList);
                });
    }
    @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    convertView = inflateView();

    Map<String, Object> history = historyList.get(position);

    setupTextViews(convertView, history);
    setupButtons(convertView, history);

    return convertView;
}
    /**
     * Inflate the view for the history adapter.
     *
     * @return the inflated view
     */
    private View inflateView() {
        return inflater.inflate(R.layout.activity_history_adapter, null);
    }


    /**
     * Setup the text views for the history item.
     *
     * @param convertView the view to setup
     * @param history the history item data
     */

    private void setupTextViews(View convertView, Map<String, Object> history) {
        TextView textView = convertView.findViewById(R.id.cardTitle);
        TextView historyStartTime = convertView.findViewById(R.id.historyStartTime);
        TextView historyEndTime = convertView.findViewById(R.id.historyEndTime);

        textView.setText("Ride #" + history.get("ride_id"));

        String date_started = formatDate(history.get("date_started").toString());
        historyStartTime.setText("Start Time: " + date_started);

        if (history.get("date_ended") != null) {
            String date_ended = formatDate(history.get("date_ended").toString());
            historyEndTime.setText("End Time: " + date_ended);
        } else {
            updateRemainingTime(history, historyEndTime);
        }
    }


    /**
     * Format the date from milliseconds to a readable string.
     *
     * @param dateInMilliseconds the date in milliseconds
     * @return the formatted date string
     */
    private String formatDate(String dateInMilliseconds) {
        Date date = new Date(Long.parseLong(dateInMilliseconds));
        return date.toString();
    }

    /**
     * Setup the buttons for the history item.
     *
     * @param convertView the view to setup
     * @param history the history item data
     */
    private void setupButtons(View convertView, Map<String, Object> history) {
         viewRideBtn = convertView.findViewById(R.id.historyViewRideBtn);
         endRideBtn = convertView.findViewById(R.id.historyEndRideBtn);
         extendRideBtn = convertView.findViewById(R.id.historyExtendRideBtn);

        extendRideBtn.setVisibility(View.GONE);

        if (isRideOngoing(history)) {
            endRideBtn.setVisibility(View.VISIBLE);
            endRideBtn.setOnClickListener(v -> endRide(history));
        } else {
            endRideBtn.setVisibility(View.INVISIBLE);
        }

        viewRideBtn.setOnClickListener(v -> viewRide(history));
    }


    /**
     * Check if the ride is ongoing.
     *
     * @param history the history item data
     * @return true if the ride is ongoing, false otherwise
     */
    private boolean isRideOngoing(Map<String, Object> history) {
        return history.get("status").toString().equals("ongoing");
    }


    /**
     * Update the remaining time for the ride.
     *
     * @param history the history item data
     * @param historyEndTime the text view to update
     */
    private void updateRemainingTime(Map<String, Object> history, TextView historyEndTime) {
        final Handler handler = new Handler();
        Date startTime = new Date(Long.parseLong(history.get("date_started").toString()));

        final Runnable updateRemainingTime = new Runnable() {
            @Override
            public void run() {
                double remainingTimeInMinutes = calculateRemainingTimeInMinutes(history, startTime);
                if (remainingTimeInMinutes <= 0) {
                    showExtendRideButton(history, historyEndTime, extendRideBtn);
                } else {
                    historyEndTime.setText("End Time: " + remainingTimeInMinutes + " minutes remaining");
                }
                handler.postDelayed(this, 30000);
            }
        };

        handler.post(updateRemainingTime);
    }



    /**
     * Calculate the remaining time in minutes for the ride.
     *
     * @param history the history item data
     * @param startTime the start time of the ride
     * @return the remaining time in minutes
     */
    private double calculateRemainingTimeInMinutes(Map<String, Object> history, Date startTime) {
        Date currentTime = new Date();
        long timeDifference = currentTime.getTime() - startTime.getTime();
        long timeDifferenceInMinutes = timeDifference / 60000;

        double extension = history.get("extension") == null ? 1 : Double.parseDouble(history.get("extension").toString());
        if (CurrentUser.vehicle_type.equalsIgnoreCase("kalesa")) {
            return 60 * extension - timeDifferenceInMinutes;
        } else {
            return 30 * extension - timeDifferenceInMinutes;
        }
    }

    /**
     * Show the extend ride button.
     *
     * @param history the history item data
     * @param historyEndTime the text view to update
     * @param extendRideBtn the button to show
     */

    private void showExtendRideButton(Map<String, Object> history, TextView historyEndTime, Button extendRideBtn){

        extendRideBtn.setVisibility(View.VISIBLE);
        extendRideBtn.setOnClickListener(v -> extendRide(history));
        historyEndTime.setText("End Time: 0 minutes remaining");
    }


    /**
     * End the ride and update the status in the database.
     *
     * @param currentRide the ride to end
     */
    public void endRide(Map<String, Object> currentRide){

        db.collection("rides").document(currentRide.get("ride_id").toString()).update("status", "completed", "date_ended", System.currentTimeMillis()).addOnCompleteListener(
                task -> {
                    computeFare(currentRide.get("vehicle_type").toString(), currentRide.get("ride_id").toString(), currentRide.get("extension").toString(), currentRide.get("passenger_quantity").toString(), currentRide.get("fare_type").toString());

                }

        );
        Toast toast = Toast.makeText(context, "Ride Ended", Toast.LENGTH_SHORT);
        toast.show();


        // Refresh the history list

    }



    /**
     * Compute the fare for a ride.
     *
     * @param vehicleType the type of vehicle used for the ride
     * @param ride_id the id of the ride
     * @param extension the extension time for the ride
     * @param passenger_quantity the number of passengers for the ride
     * @param fare_type the type of fare for the ride
     */
    public void computeFare(String vehicleType, String ride_id, String extension, String passenger_quantity, String fare_type) {
        Double BASE_RATE = 0.0;
        Double PER_MINUTE_RATE = 0.0;


        if (vehicleType.equalsIgnoreCase("kalesa")) {
            System.out.println("Kalesa");
            if (fare_type.equalsIgnoreCase("regular")) {
                BASE_RATE = 1000.0;
                PER_MINUTE_RATE = BASE_RATE / 60;
            } else if (fare_type.equalsIgnoreCase("student") || fare_type.equalsIgnoreCase("senior") || fare_type.equalsIgnoreCase("pwd")) {
                BASE_RATE = 800.0;
                PER_MINUTE_RATE = BASE_RATE / 60;
            }

        } else if (vehicleType.equalsIgnoreCase("pedicab")) {
            System.out.println("Pedicab");

            if (fare_type.equalsIgnoreCase("regular")) {
                BASE_RATE = 400.0;
                PER_MINUTE_RATE = BASE_RATE / 60;
            } else if (fare_type.equalsIgnoreCase("student") || fare_type.equalsIgnoreCase("senior") || fare_type.equalsIgnoreCase("pwd")) {
                BASE_RATE = 320.0;
                PER_MINUTE_RATE = BASE_RATE / 60;
            }

        } else if (vehicleType.equalsIgnoreCase("tricycle")) {
            System.out.println("Tricycle");
            if (fare_type.equalsIgnoreCase("regular")) {
                BASE_RATE = 200.0;
                PER_MINUTE_RATE = BASE_RATE / 30;
            } else if (fare_type.equalsIgnoreCase("student") || fare_type.equalsIgnoreCase("senior") || fare_type.equalsIgnoreCase("pwd")) {
                BASE_RATE = 120.0;
                PER_MINUTE_RATE = BASE_RATE / 30;
            }

        }

        



        System.out.println("Base Rate: " + BASE_RATE);
        System.out.println("Extension: " + extension);
        System.out.println("Passenger Quantity: " + passenger_quantity);
        System.out.println("Ride ID: " + ride_id);

        Map<String, Object> currentRide = new HashMap<>();
        Double finalBASE_RATE = BASE_RATE;

        Double finalPER_MINUTE_RATE = PER_MINUTE_RATE;
        db.collection("rides").document(ride_id).get().addOnSuccessListener(documentSnapshot -> {
            currentRide.put("date_started", documentSnapshot.get("date_started"));
            currentRide.put("date_ended", documentSnapshot.get("date_ended"));
            double startTime = Double.parseDouble(currentRide.get("date_started").toString());
            double endTime = Double.parseDouble(currentRide.get("date_ended").toString());
            double duration = endTime - startTime;
            double durationInMinutes = duration / 60000;
            double fare = calculateFare(finalBASE_RATE, finalPER_MINUTE_RATE, extension, passenger_quantity, durationInMinutes);
            System.out.println("Fare: " + fare);
            db.collection("rides").document(ride_id).update("fare", fare);
            db.collection("rides").document(ride_id).update("duration", durationInMinutes);
            // refresh the history list
            getUpdatedHistory();
        });



    }


    /**
     * Calculate the fare based on various parameters.
     *
     * @param BASE_RATE the base rate for the ride
     * @param PER_MINUTE_RATE the per minute rate for the ride
     * @param extension the extension time for the ride
     * @param passenger_quantity the number of passengers for the ride
     * @param durationInMinutes the duration of the ride in minutes
     * @return the calculated fare
     */
    public double calculateFare(Double BASE_RATE, Double PER_MINUTE_RATE, String extension, String passenger_quantity, double durationInMinutes) {

        double timeElapsed = 0;


        // 60 minutes for kalesa

        // 65 minutes

        // 1 extensions

        // 5 minutes

        // * PER_MINUTE_RATE


        // 60

        // 60

        // 30



        double fare = 0;


        System.out.println("Duration in minutes: " + durationInMinutes);
        System.out.println("Extension: " + extension);
        System.out.println("Passenger Quantity: " + passenger_quantity);
        System.out.println("Base Rate: " + BASE_RATE);
        System.out.println("Per Minute Rate: " + PER_MINUTE_RATE);
        System.out.println("Vehicle Type: " + CurrentUser.vehicle_type);
        if (CurrentUser.vehicle_type.equalsIgnoreCase("kalesa")){
             timeElapsed = durationInMinutes - (60 * Double.parseDouble(extension));

        }
        else{
            timeElapsed = durationInMinutes - (30 * Double.parseDouble(extension));

        }

        if (timeElapsed > 0){
            fare = ( BASE_RATE + (timeElapsed * PER_MINUTE_RATE) ) * Double.parseDouble(passenger_quantity);
        }
        else{
            fare = BASE_RATE * Double.parseDouble(passenger_quantity);
        }



        return fare;
    }

    /**
     * View the details of a ride.
     *
     * @param currentRide the ride to view
     */
    public void viewRide(Map<String, Object> currentRide){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Ride Details");
        // Convert duration from milliseconds to minutes
        double duration = Double.parseDouble(currentRide.get("duration").toString()) / 60000;
        long roundedDuration = Math.round(duration);
        ViewRideFragment viewRideFragment = new ViewRideFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentRide", (HashMap<String, Object>) currentRide);
        viewRideFragment.setArguments(bundle);
        builder.setView(viewRideFragment.onCreateView(inflater, null, null));
        builder.show();


        }
    /**
     * Extend the duration of a ride.
     *
     * @param currentRide the ride to extend
     */
    public void extendRide(Map<String, Object> currentRide){
        Double currentExtension = Double.parseDouble(currentRide.get("extension").toString());
        Double newExtension = currentExtension + 1;
        db.collection("rides").document(currentRide.get("ride_id").toString()).update("extension", newExtension);
           Toast toast = Toast.makeText(context, "Ride Extended", Toast.LENGTH_SHORT);
    }



}