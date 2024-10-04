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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutesAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, Object>> historyList; // replace String with your actual data type
    private LayoutInflater inflater;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button extendRideBtn, endRideBtn, viewRideBtn;

    public RoutesAdapter(Context context, List<Map<String,Object>> historyList) {
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
        convertView = inflateView();

        Map<String, Object> history = historyList.get(position);


        return convertView;
    }
    /**
     * Inflate the view for the history adapter.
     *
     * @return the inflated view
     */
    private View inflateView() {
        return inflater.inflate(R.layout.fragment_routes_adapter, null);
    }


    /**
     * Setup the text views for the history item.
     *
     * @param convertView the view to setup
     * @param history the history item data
     */


}