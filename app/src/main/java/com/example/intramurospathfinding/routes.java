package com.example.intramurospathfinding;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link routes#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class routes extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String jsonResponse;
    ArrayList<HashMap<String, Object>> routesList  = new ArrayList<HashMap<String, Object>>();
    LatLng  origin, destination;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment routes.
     */
    // TODO: Rename and change types and number of parameters
    public static routes newInstance(String param1, String param2) {
        routes fragment = new routes();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    public void setJsonResponse(String jsonResponse, LatLng origin, LatLng destination){

        this.jsonResponse = jsonResponse;
        this.origin = origin;
        this.destination = destination;
    }

    public routes() {
        // Required empty public constructor
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
        View v =  inflater.inflate(R.layout.fragment_routes, container, false);

        ListView routes_list = (ListView) v.findViewById(R.id.routes_list);
        Bundle bundle = getArguments();


        System.out.println("JSON RESPONSE: " + jsonResponse);

        try {
            JSONObject jsonResponseObject = new JSONObject(jsonResponse);
            JSONArray pathsArray = jsonResponseObject.getJSONArray("paths");

            for (int i = 0; i < pathsArray.length(); i++) {
                JSONObject path = pathsArray.getJSONObject(i);

                String pathDistance = path.getString("distance");
                String pathDuration = path.getString("time");
                String pathInstructions = path.getString("instructions");

                Map<String, Object> pathMap = new HashMap<String, Object>();
                pathMap.put("distance", pathDistance);
                pathMap.put("duration", pathDuration);
                pathMap.put("instructions", pathInstructions);
                pathMap.put("path", decodePolyline(path.getString("points")));

                routesList.add((HashMap<String, Object>) pathMap);
            }


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        System.out.println("ROUTES LIST: " + routesList);


        RoutesAdapter routesAdapter = new RoutesAdapter(v.getContext(), routesList, origin, destination);
        routes_list.setAdapter(routesAdapter);

        return v;
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

}