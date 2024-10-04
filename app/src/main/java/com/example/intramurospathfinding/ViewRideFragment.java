package com.example.intramurospathfinding;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewRideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewRideFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ViewRideFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewRideFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewRideFragment newInstance(String param1, String param2) {
        ViewRideFragment fragment = new ViewRideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    Map<String, Object> currentRide;

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View v =  inflater.inflate(R.layout.fragment_view_ride, container, false);
    Bundle bundle = getArguments();

    if (bundle != null) {
        currentRide = (Map<String, Object>) bundle.getSerializable("currentRide");
        setupViews(v, bundle);
        populateData(v);
    }

    return v;
}

/**
 * This method is used to setup the views and their listeners.
 *
 * @param v The inflated view.
 * @param bundle The arguments passed to the fragment.
 */
private void setupViews(View v, Bundle bundle) {

    FragmentContainerView previewRideMap = v.findViewById(R.id.previewRideMap);
    FragmentManager fragmentManager = getChildFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    PreviewRideMap previewRideMapFragment = new PreviewRideMap();
    previewRideMapFragment.setArguments(bundle);
    fragmentTransaction.add(previewRideMap.getId(), previewRideMapFragment);
    fragmentTransaction.commit();

}

/**
 * This method is used to populate the data into the views.
 *
 * @param v The inflated view.
 */
private void populateData(View v) {
    TextView distance = v.findViewById(R.id.distance);
    TextView dateStarted  = v.findViewById(R.id.dateStarted);
    TextView dateEnded = v.findViewById(R.id.dateEnded);
    TextView status = v.findViewById(R.id.status);
    TextView fare = v.findViewById(R.id.fare);

    long timestamp = Long.parseLong(currentRide.get("date_started").toString());
    Date date = new Date(timestamp);
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); // Change this format to whatever you need
    String dateString = formatter.format(date);

    if (currentRide.get("distance") != null){
        distance.setText("Distance: " + currentRide.get("distance").toString() + " meters");
    } else {
        distance.setText("Distance:");
    }

    dateStarted.setText("Date Started: " + dateString);

    if (currentRide.get("date_ended") != null) {
        timestamp = Long.parseLong(currentRide.get("date_ended").toString());
        date = new Date(timestamp);
        dateString = formatter.format(date);
        dateEnded.setText("Date Ended: " + dateString);
    } else {
        dateEnded.setText("Date Ended: Ongoing");
    }

    status.setText("Status: " + currentRide.get("status").toString());

    double fareValue = Double.parseDouble(currentRide.get("fare").toString());
    // Convert to two decimal places
    fareValue = Math.round(fareValue * 100.0) / 100.0;
    fare.setText("Fare: " + fareValue);
}









}

