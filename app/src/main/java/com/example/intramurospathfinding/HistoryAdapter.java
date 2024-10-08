package com.example.intramurospathfinding;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryAdapter extends BaseAdapter {

    private Context context;
    private static List<Map<String, Object>> historyList; // replace String with your actual data type
    private LayoutInflater inflater;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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
        historyList = newHistoryList;
        notifyDataSetChanged();
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
                        ride.put("extension", documentSnapshot.get("extension"));
                        ride.put("regular_passenger_quantity", documentSnapshot.get("regular_passenger_quantity"));
                        ride.put("student_passenger_quantity", documentSnapshot.get("student_passenger_quantity"));
                        ride.put("senior_passenger_quantity", documentSnapshot.get("senior_passenger_quantity"));
                        ride.put("pwd_passenger_quantity", documentSnapshot.get("pwd_passenger_quantity"));
                        ride.put("vehicle_type", documentSnapshot.get("vehicle_type"));
                        ride.put("path", documentSnapshot.get("path"));
                        ride.put("ride_title", documentSnapshot.get("ride_title"));


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

        textView.setText(history.get("ride_title").toString());

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
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        return formatter.format(date);
    }






    private void setupButtons(View convertView, Map<String, Object> history) {
        viewRideBtn = convertView.findViewById(R.id.historyViewRideBtn);
        endRideBtn = convertView.findViewById(R.id.historyEndRideBtn);

        if (isRideOngoing(history)) {
            endRideBtn.setVisibility(View.VISIBLE);
            endRideBtn.setOnClickListener(v -> endRide(history));
        } else {
            endRideBtn.setVisibility(View.GONE);
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
                if(remainingTimeInMinutes <= 0) {
                    extendRide(history);
                }
                else if (Integer.parseInt(history.get("extension").toString()) >= 1) {
                    // add how many times the extension is
                    historyEndTime.setText("End Time: " + remainingTimeInMinutes + " minutes remaining " + " (Extended) " + history.get("extension").toString() + "x");
                }

                else {
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
        // every extension is 10 minutes
        int extension = Integer.parseInt(history.get("extension").toString());
        long timeDifference = (currentTime.getTime() - startTime.getTime()) + (extension * 600000);
        long timeDifferenceInMinutes = timeDifference / 60000;

        if (CurrentUser.vehicle_type.equalsIgnoreCase("kalesa")) {
            return 60  - timeDifferenceInMinutes;
        } else {
            return 30  - timeDifferenceInMinutes;
        }
    }





    /**
     * End the ride and update the status in the database.
     *
     * @param currentRide the ride to end
     */
    public void endRide(Map<String, Object> currentRide){

        db.collection("rides").document(currentRide.get("ride_id").toString()).update("status", "completed", "date_ended", System.currentTimeMillis(), "duration", computeDuration(currentRide.get("date_started"))).addOnCompleteListener(
                task -> {
                    computeFare(currentRide.get("vehicle_type").toString(), currentRide.get("ride_id").toString(), currentRide.get("extension").toString(), currentRide);

                }

        );
        Toast toast = Toast.makeText(context, "Ride Ended", Toast.LENGTH_SHORT);
        toast.show();


        // Refresh the history list

    }

    public long computeDuration(Object date_started){
        long startTime = Long.parseLong(date_started.toString());
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        return duration;
    }



    /**
     * Compute the fare for a ride.
     *
     * @param vehicleType the type of vehicle used for the ride
     * @param ride_id the id of the ride
     * @param extension the extension time for the ride

     */
    public void computeFare(String vehicleType, String ride_id, String extension, Map<String, Object> history){
        Double BASE_RATE = 0.0;
        Double PER_MINUTE_RATE = 0.0;
        Double SPECIAL_BASE_RATE = 0.0;
        Double SPECIAL_PER_MINUTE_RATE = 0.0;

        Double[] rate = computeBaseFare(vehicleType);
        BASE_RATE = rate[0];
        PER_MINUTE_RATE = rate[1];
        SPECIAL_BASE_RATE = rate[2];
        SPECIAL_PER_MINUTE_RATE = rate[3];



        Map<String, Object> currentRide = new HashMap<>();
        Double finalBASE_RATE = BASE_RATE;

        Double finalPER_MINUTE_RATE = PER_MINUTE_RATE;
        Double finalSPECIAL_BASE_RATE = SPECIAL_BASE_RATE;
        Double finalSPECIAL_PER_MINUTE_RATE = SPECIAL_PER_MINUTE_RATE;
        db.collection("rides").document(ride_id).get().addOnSuccessListener(documentSnapshot -> {
            currentRide.put("date_started", documentSnapshot.get("date_started"));
            currentRide.put("date_ended", documentSnapshot.get("date_ended"));
            double startTime = Double.parseDouble(currentRide.get("date_started").toString());
            double endTime = Double.parseDouble(currentRide.get("date_ended").toString());
            double duration = endTime - startTime;
            double durationInMinutes = duration / 60000;
            double fare = calculateFare(finalBASE_RATE, finalPER_MINUTE_RATE, finalSPECIAL_BASE_RATE, finalSPECIAL_PER_MINUTE_RATE, extension, history.get("regular_passenger_quantity").toString(), history.get("student_passenger_quantity").toString(),history.get("senior_passenger_quantity").toString(), history.get("pwd_passenger_quantity").toString(),  durationInMinutes);
            System.out.println("Fare: " + fare);
            db.collection("rides").document(ride_id).update("fare", fare).addOnCompleteListener(task -> {
                db.collection("rides").document(ride_id).update("duration", durationInMinutes).addOnCompleteListener(t -> {
                    getUpdatedHistory();
                });
            });

        });

    }


    public static Double[] computeBaseFare(String vehicleType){
        Double BASE_RATE = 0.0;
        Double PER_MINUTE_RATE = 0.0;
        Double SPECIAL_BASE_RATE = 0.0;
        Double SPECIAL_PER_MINUTE_RATE = 0.0;
        if (vehicleType.equalsIgnoreCase("kalesa")) {
            System.out.println("Kalesa");
                BASE_RATE = 1000.0;
                PER_MINUTE_RATE = BASE_RATE / 60;

                SPECIAL_BASE_RATE = 800.0;
                SPECIAL_PER_MINUTE_RATE = SPECIAL_BASE_RATE / 60;

        } else if (vehicleType.equalsIgnoreCase("pedicab")) {
            System.out.println("Pedicab");

                BASE_RATE = 400.0;
                PER_MINUTE_RATE = BASE_RATE / 60;

                SPECIAL_BASE_RATE = 320.0;
                SPECIAL_PER_MINUTE_RATE = SPECIAL_BASE_RATE / 60;


        } else if (vehicleType.equalsIgnoreCase("tricycle")) {
            System.out.println("Tricycle");

                BASE_RATE = 200.0;
                PER_MINUTE_RATE = BASE_RATE / 30;

                SPECIAL_BASE_RATE = 120.0;
                SPECIAL_PER_MINUTE_RATE = SPECIAL_BASE_RATE / 30;

        }

        return new Double[]{BASE_RATE, PER_MINUTE_RATE, SPECIAL_BASE_RATE, SPECIAL_PER_MINUTE_RATE};

    }
    /**
     * Calculate the fare based on various parameters.
     *
     * @param BASE_RATE the base rate for the ride
     * @param PER_MINUTE_RATE the per minute rate for the ride
   =
     * @param durationInMinutes the duration of the ride in minutes
     * @return the calculated fare
     */
    public static double calculateFare(Double BASE_RATE, Double PER_MINUTE_RATE, Double SPECIAL_BASE_RATE, Double SPECIAL_PER_MINUTE_RATE, String extension, String regular_passenger_quantity, String student_passenger_quantity, String senior_passenger_quantity, String pwd_passenger_quantity, double durationInMinutes) {

        double timeElapsed = 0;



        double fare = 0;



        if (CurrentUser.vehicle_type.equalsIgnoreCase("kalesa") || CurrentUser.vehicle_type.equalsIgnoreCase("pedicab")){
            if (Double.parseDouble(extension) >= 1)
                timeElapsed = durationInMinutes - (60 * Double.parseDouble(extension));
            else {
                timeElapsed = durationInMinutes  - 60;
            }


        }
        else {
            if (Double.parseDouble(extension) >= 1)
                timeElapsed = durationInMinutes - (30 * Double.parseDouble(extension));
            else {
                timeElapsed = durationInMinutes  - 30;
            }

        }

        if (timeElapsed > 0){

            fare = ( BASE_RATE + (timeElapsed * PER_MINUTE_RATE) ) * Double.parseDouble(regular_passenger_quantity) + ( SPECIAL_BASE_RATE + (timeElapsed * SPECIAL_PER_MINUTE_RATE) ) * Double.parseDouble(student_passenger_quantity) + ( SPECIAL_BASE_RATE + (timeElapsed * SPECIAL_PER_MINUTE_RATE) ) * Double.parseDouble(senior_passenger_quantity) + ( SPECIAL_BASE_RATE + (timeElapsed * SPECIAL_PER_MINUTE_RATE) ) * Double.parseDouble(pwd_passenger_quantity);

        }
        else{
            fare = BASE_RATE * Double.parseDouble(regular_passenger_quantity) + SPECIAL_BASE_RATE * Double.parseDouble(student_passenger_quantity) + SPECIAL_BASE_RATE * Double.parseDouble(senior_passenger_quantity) + SPECIAL_BASE_RATE * Double.parseDouble(pwd_passenger_quantity);
        }



        return fare;
    }

    /**
     * View the details of a ride.
     *
     * @param currentRide the ride to view
     */
    public void viewRide(Map<String, Object> currentRide){
        System.out.println(currentRide);

        Drawable dateDrawable = ContextCompat.getDrawable(context, R.drawable.date);
        dateDrawable.setBounds(0, 0, 60, 60);

        Drawable distanceDrawable = ContextCompat.getDrawable(context, R.drawable.distance);
        distanceDrawable.setBounds(0, 0, 60, 60);

        Drawable fareDrawable = ContextCompat.getDrawable(context, R.drawable.coins);
        fareDrawable.setBounds(0, 0, 60, 60);

        TextView dateStarted = ((MainActivity) context).findViewById(R.id.dateStarted);
        TextView dateEnded = ((MainActivity) context).findViewById(R.id.dateEnded);
        TextView distance = ((MainActivity) context).findViewById(R.id.distance);
        TextView fare = ((MainActivity) context).findViewById(R.id.fare);
        TextView status = ((MainActivity) context).findViewById(R.id.status);
        TextView regularPassengerQuantity = ((MainActivity) context).findViewById(R.id.regularPassengerQuantity);
        TextView studentPassengerQuantity = ((MainActivity) context).findViewById(R.id.studentPassengerQuantity);
        TextView seniorPassengerQuantity = ((MainActivity) context).findViewById(R.id.seniorPassengerQuantity);
        TextView pwdPassengerQuantity = ((MainActivity) context).findViewById(R.id.pwdPassengerQuantity);

        TextView duration = ((MainActivity) context).findViewById(R.id.duration);




        FragmentContainerView previewRideMap = ((MainActivity) context).findViewById(R.id.previewRideMap);

        dateStarted.setCompoundDrawables(dateDrawable, null, null, null);
        dateStarted.setText(formatDate(currentRide.get("date_started").toString()));
        if (currentRide.get("date_ended") != null) {
            dateEnded.setCompoundDrawables(dateDrawable, null, null, null);
            dateEnded.setText(formatDate(currentRide.get("date_ended").toString()));
        } else {
            dateEnded.setText("Date Ended: Ongoing");
        }

        distance.setCompoundDrawables(distanceDrawable, null, null, null);
        distance.setText("Distance: " + currentRide.get("distance").toString() + " m");
        double fareValue = Double.parseDouble(currentRide.get("fare").toString());
        String fareFormatted = String.format("%.2f", fareValue);
        fare.setCompoundDrawables(fareDrawable, null, null, null);
        fare.setText(fareFormatted + " PHP");

        status.setText("Status: " + currentRide.get("status").toString());


        if (currentRide.get("status").toString().equalsIgnoreCase("completed")) {


            String rideDuration = currentRide.get("duration").toString();
            Double rideDurationDouble;
            String rideDurationString;
            if (CurrentUser.vehicle_type.equalsIgnoreCase("kalesa")) {
                 rideDurationDouble = Double.parseDouble(rideDuration);
                 if(rideDurationDouble - 60 > 0){
                     Double rideDurationElapsed = rideDurationDouble - 60;
                     rideDurationString = String.format("%.2f", rideDurationDouble) + "minutes + " + String.format("%.2f", rideDurationElapsed) + " minutes (Elapsed)";
                 }
                 else{
                     rideDurationString = String.format("%.2f", rideDurationDouble);
                 }

                regularPassengerQuantity.setText("Regular: " + currentRide.get("regular_passenger_quantity").toString());
                studentPassengerQuantity.setText("Student: " + currentRide.get("student_passenger_quantity").toString());
                seniorPassengerQuantity.setText("Senior: " + currentRide.get("senior_passenger_quantity").toString());
                pwdPassengerQuantity.setText("PWD: " + currentRide.get("pwd_passenger_quantity").toString());
                duration.setText("Duration: " + rideDurationString + " minutes");

                duration.setText("Duration: " + rideDurationString + " minutes");
            } else {
                rideDurationDouble = Double.parseDouble(rideDuration);
                if(rideDurationDouble - 60 > 0){
                    Double rideDurationElapsed = rideDurationDouble - 30;
                    rideDurationString = String.format("%.2f", rideDurationDouble) + "minutes + " + String.format("%.2f", rideDurationElapsed) + " minutes (Elapsed)";
                }
                else{
                    rideDurationString = String.format("%.2f", rideDurationDouble);
                }

                regularPassengerQuantity.setText("Regular: " + currentRide.get("regular_passenger_quantity").toString());
                studentPassengerQuantity.setText("Student: " + currentRide.get("student_passenger_quantity").toString());
                seniorPassengerQuantity.setText("Senior: " + currentRide.get("senior_passenger_quantity").toString());
                pwdPassengerQuantity.setText("PWD: " + currentRide.get("pwd_passenger_quantity").toString());
                duration.setText("Duration: " + rideDurationString + " minutes");

            }

        } else {

            regularPassengerQuantity.setVisibility(View.GONE);
            studentPassengerQuantity.setVisibility(View.GONE);
            seniorPassengerQuantity.setVisibility(View.GONE);
            pwdPassengerQuantity.setVisibility(View.GONE);
            duration.setVisibility(View.GONE);

        }

        // Show the preview map

        Fragment fragment = new PreviewRideMap();
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentRide", (HashMap<String, Object>) currentRide);
        fragment.setArguments(bundle);
        ((MainActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.previewRideMap, fragment).commit();




        // Trigger the bottom sheet to be revealed

        FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
        View bottomSheet = ((MainActivity) context).findViewById(R.id.historyDetailBottomSheet);
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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