package com.example.intramurospathfinding;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RoutesAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<HashMap<String, Object>> historyList; // replace String with your actual data type // replace String with your actual data type
    private LayoutInflater inflater;
    LatLng origin, destination;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button extendRideBtn, endRideBtn, viewRideBtn;

    public RoutesAdapter(Context context, ArrayList<HashMap<String, Object>> historyList, LatLng origin, LatLng destination) {
        this.context = context;
        this.historyList = historyList;
        this.inflater = LayoutInflater.from(context);
        this.origin = origin;
        this.destination = destination;

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


    public String convertMilisecondsToMinutes(long miliseconds) {
        long minutes = miliseconds / 60000;
        long seconds = (miliseconds % 60000) / 1000;
        // if CurrentUser.vehicle_type is kalesa add 30 minutes, if tricycle 15 minutes, if pedicab 50 minutes



        return minutes + "m " + seconds + "s";
    }





    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflateView();
        }

        HashMap<String, Object> routeOption = getRouteOption(position);

        setupTextViews(convertView, routeOption, position);

        List<HashMap<String, Object>> instructionsList = convertInstructions(routeOption);

        getFirstAndLastInstruction(convertView, instructionsList);

        setupGoButton(convertView, routeOption);

        return convertView;
    }
    private HashMap<String, Object> getRouteOption(int position) {
        return historyList.get(position);
    }

    private void setupTextViews(View convertView, HashMap<String, Object> routeOption, int position) {
        TextView rideDistance = convertView.findViewById(R.id.rideDistance);
        TextView rideETT = convertView.findViewById(R.id.rideETT);
        TextView routeNumber = convertView.findViewById(R.id.routeNumber);

        rideDistance.setText(routeOption.get("distance").toString() + "m");
        rideETT.setText(convertMilisecondsToMinutes(Long.parseLong(routeOption.get("duration").toString())));
        routeNumber.setText("Route " + (position + 1));
    }

    private List<HashMap<String, Object>> convertInstructions(HashMap<String, Object> routeOption) {
        JSONArray instructionsArray = null;
        try {
            instructionsArray = new JSONArray(routeOption.get("instructions").toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        List<HashMap<String, Object>> instructionsList = new ArrayList<>();
        for (int i = 0; i < instructionsArray.length(); i++) {
            JSONObject instructionObject = null;
            try {
                instructionObject = instructionsArray.getJSONObject(i);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            HashMap<String, Object> instructionMap = new HashMap<>();
            Iterator<String> keys = instructionObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = null;
                try {
                    value = instructionObject.get(key);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                instructionMap.put(key, value);
            }
            instructionsList.add(instructionMap);
        }
        return instructionsList;
    }

    private void getFirstAndLastInstruction(View convertView, List<HashMap<String, Object>> instructionsList) {
        TextView rideName = convertView.findViewById(R.id.rideName);

        HashMap<String, Object> firstInstruction = instructionsList.get(1);
        HashMap<String, Object> lastInstruction = instructionsList.get(instructionsList.size() - 2);
        String rideTitle = firstInstruction.get("street_name") + " to " + lastInstruction.get("street_name");
        rideName.setText(rideTitle);
    }

    private String getRideTitle(List<HashMap<String, Object>> instructionsList) {

        HashMap<String, Object> firstInstruction = instructionsList.get(1);
        HashMap<String, Object> lastInstruction = instructionsList.get(instructionsList.size() - 2);

        return firstInstruction.get("street_name") + " to " + lastInstruction.get("street_name");
    }




    private void setupGoButton(View convertView, HashMap<String, Object> routeOption) {
        Button goBtn = convertView.findViewById(R.id.goBtn);

        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new RouteListPreviewMap();
                Bundle bundle = new Bundle();
                bundle.putString("path", routeOption.get("path").toString());
                bundle.putDouble("originLatitude", origin.latitude);
                bundle.putDouble("originLongitude", origin.longitude);
                bundle.putDouble("destinationLatitude", destination.latitude);
                bundle.putDouble("destinationLongitude", destination.longitude);
                bundle.putString("distance", routeOption.get("distance").toString());
                bundle.putString("duration", routeOption.get("duration").toString());
                bundle.putString("title", getRideTitle(convertInstructions(routeOption)));
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
                fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, fragment).commit();
            }
        });
    }
        /**
         * Inflate the view for the history adapter.
         *
         * @return the inflated view
         */
        private View inflateView() {
            return inflater.inflate(R.layout.fragment_routes_adapter, null);
        }




}