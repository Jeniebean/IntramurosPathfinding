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
    convertView = inflater.inflate(R.layout.activity_history_adapter, null);

    TextView textView = convertView.findViewById(R.id.cardTitle);
    TextView historyStartTime = convertView.findViewById(R.id.historyStartTime);
    TextView historyEndTime = convertView.findViewById(R.id.historyEndTime);
    Button viewRideBtn = convertView.findViewById(R.id.historyViewRideBtn);
    Button endRideBtn = convertView.findViewById(R.id.historyEndRideBtn);

    Map<String, Object> history = historyList.get(position);


    textView.setText("Ride #" + history.get("ride_id"));

    String date_started = history.get("date_started").toString();
    Date date = new Date(Long.parseLong(date_started));
    date_started = date.toString();
    historyStartTime.setText("Start Time: " + date_started);

    if (history.get("status").toString().equals("ongoing")) {
        endRideBtn.setVisibility(View.VISIBLE);
        endRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endRide(history);
            }
        });
    } else {
        endRideBtn.setVisibility(View.INVISIBLE);
    }

    viewRideBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            viewRide(history);
        }
    });

    if (history.get("date_ended") != null) {
        String date_ended = history.get("date_ended").toString();
        date = new Date(Long.parseLong(date_ended));
        date_ended = date.toString();
        historyEndTime.setText("End Time: " + date_ended);
    } else {
        // Compute 30 minutes from the start time
        final Handler handler = new Handler();
        Date startTime = new Date(Long.parseLong(history.get("date_started").toString()));
        final Runnable updateRemainingTime = new Runnable() {
            @Override
            public void run() {
                // Get the current time
                Date currentTime = new Date();

                // Calculate the difference between the current time and the start time
                long timeDifference = currentTime.getTime() - startTime.getTime();

                // Convert this difference to minutes
                long timeDifferenceInMinutes = timeDifference / 60000;

                // Calculate the remaining time by subtracting the time difference from 30 minutes
                long remainingTimeInMinutes = 30 - timeDifferenceInMinutes;

                // Update the text view
                historyEndTime.setText("End Time: " + remainingTimeInMinutes + " minutes remaining");

                if (remainingTimeInMinutes <= 0) {
                    // End the ride
                    endRide(history);
                }
                // Schedule the next update in 30 seconds
                handler.postDelayed(this, 30000);
            }
        };

// Start the initial runnable task by posting through the handler
handler.post(updateRemainingTime);



    }

    return convertView;
}

    public void endRide(Map<String, Object> currentRide){

        db.collection("rides").document(currentRide.get("ride_id").toString()).update("status", "completed", "date_ended", System.currentTimeMillis()).addOnCompleteListener(
                task -> {
                    computeFare("bike", currentRide.get("ride_id").toString());

                }
        );
        Toast toast = Toast.makeText(context, "Ride Ended", Toast.LENGTH_SHORT);
        toast.show();


        // Refresh the history list
        getUpdatedHistory();
    }

    public void computeFare(String vehicleType, String ride_id){
        Double BASE_RATE = 200.0;
        Double PER_MINUTE_RATE = (BASE_RATE * 2)  / 60;

        Map<String, Object> currentRide = new HashMap<>();
        db.collection("rides").document(ride_id).get().addOnSuccessListener(documentSnapshot -> {
            currentRide.put("date_started", documentSnapshot.get("date_started"));
            currentRide.put("date_ended", documentSnapshot.get("date_ended"));
            double startTime = Double.parseDouble(currentRide.get("date_started").toString());
            double endTime = Double.parseDouble(currentRide.get("date_ended").toString());
            double duration = endTime - startTime;
            double durationInMinutes = duration / 60000;
            double fare = (durationInMinutes * PER_MINUTE_RATE);
            db.collection("rides").document(ride_id).update("fare", fare);
            db.collection("rides").document(ride_id).update("duration", durationInMinutes);

        });



    }


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



}