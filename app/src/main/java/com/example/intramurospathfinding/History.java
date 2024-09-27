package com.example.intramurospathfinding;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link History#newInstance} factory method to
 * create an instance of this fragment.
 */
public class History extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public History() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment History.
     */
    // TODO: Rename and change types and number of parameters
    public static History newInstance(String param1, String param2) {
        History fragment = new History();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history, container, false);

        ListView listView = (ListView) v.findViewById(R.id.history_list);

        getHistory(listView, v);


        return v;
    }

  public void getHistory(ListView listView, View v){
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Map<String, Object>> userRides = new ArrayList<>();
    db.collection("rides")
        .whereEqualTo("user", CurrentUser.user_id).orderBy("date_started", Query.Direction.DESCENDING)
        .get()
            // sort by date_started

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
                ride.put("path", documentSnapshot.get("path") == null ? new ArrayList<>() : documentSnapshot.get("path") );


                userRides.add(ride);
            }
            HistoryAdapter historyAdapter = new HistoryAdapter(v.getContext(), userRides);
            listView.setAdapter(historyAdapter);
            System.out.println("User Rides: " + userRides);
        });
}
}