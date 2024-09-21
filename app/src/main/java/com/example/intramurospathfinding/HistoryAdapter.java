package com.example.intramurospathfinding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class HistoryAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> historyList; // replace String with your actual data type
    private LayoutInflater inflater;
    int count = 0;
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

@Override
public View getView(int position, View convertView, ViewGroup parent) {
    convertView = inflater.inflate(R.layout.activity_history_adapter, null);

    TextView textView = convertView.findViewById(R.id.cardTitle);
    TextView historyStartTime = convertView.findViewById(R.id.historyStartTime);
    TextView historyEndTime = convertView.findViewById(R.id.historyEndTime);
    Button viewRideBtn = convertView.findViewById(R.id.historyViewRideBtn);
    Button endRideBtn = convertView.findViewById(R.id.historyEndRideBtn);

    Map<String, Object> history = historyList.get(position);
    count++;

    textView.setText("Ride #" + String.valueOf(count));

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

    if (history.get("date_ended") != null) {
        String date_ended = history.get("date_ended").toString();
        date = new Date(Long.parseLong(date_ended));
        date_ended = date.toString();
        historyEndTime.setText("End Time: " + date_ended);
    } else {
        historyEndTime.setText("End Time: Ongoing");
    }

    return convertView;
}
    public void endRide(Map<String, Object> currentRide){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rides").document(currentRide.get("ride_id").toString()).update("status", "completed", "date_ended", System.currentTimeMillis());
        Toast toast = Toast.makeText(context, "Ride Ended", Toast.LENGTH_SHORT);
        toast.show();

    }
}