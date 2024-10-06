package com.example.intramurospathfinding;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Registration#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Registration extends Fragment {

    View v;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Registration() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Registration.
     */
    // TODO: Rename and change types and number of parameters
    public static Registration newInstance(String param1, String param2) {
        Registration fragment = new Registration();
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


    public static boolean validateInput(String firstname, String lastname, String password) {
        return isNameValid(firstname) && isNameValid(lastname) && isPasswordValid(password);
    }

    public static boolean isNameValid(String name) {
        return name.matches("[a-zA-Z]*") && name.length() <= 255 && name.length() > 0;
    }

    public static boolean isPasswordValid(String password) {
        return password.length() <= 255;
    }

    private Map<String, Object> createUser(String firstname, String lastname, String username, String password, String vehicleType) {
        Map<String, Object> user = new HashMap<>();
        user.put("firstname", firstname);
        user.put("lastname", lastname);
        user.put("username", username);
        user.put("password", password);
        user.put("vehicle_type", vehicleType);
        return user;
    }

    private void addUserToDatabase(Map<String, Object> user) {
        db.collection("users").add(user);
    }

    private void navigateToLogin() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Login()).commit();
    }


    Button submitBtn;
    RadioGroup vehicle_type;
    RadioButton kalesaRadioButton, pedicabRadioButton, tricycleRadioButton;
    EditText firstname,lastname, username, password, confirmPassword;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_registration, container, false);

        submitBtn = (Button) v.findViewById(R.id.submitBtn);
        firstname = (EditText) v.findViewById(R.id.firstname);
        lastname = (EditText) v.findViewById(R.id.lastname);
        username = (EditText) v.findViewById(R.id.username);
        password = (EditText) v.findViewById(R.id.password);
        confirmPassword = (EditText) v.findViewById(R.id.confirmPassword);
        vehicle_type = (RadioGroup) v.findViewById(R.id.vehicle_type);
        kalesaRadioButton = (RadioButton) v.findViewById(R.id.kalesaRadioButton);
        pedicabRadioButton = (RadioButton) v.findViewById(R.id.pedicabRadioButton);
        tricycleRadioButton = (RadioButton) v.findViewById(R.id.tricycleRadioButton);

        kalesaRadioButton.setSelected(true);




        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstnameStr = firstname.getText().toString();
                String lastnameStr = lastname.getText().toString();
                String usernameStr = username.getText().toString();
                String passwordStr = password.getText().toString();
                String confirmPasswordStr = confirmPassword.getText().toString();

                if (passwordStr.equals(confirmPasswordStr) && validateInput(firstnameStr, lastnameStr, passwordStr)) {
                    String vehicleType = getVehicleType();
                    Map<String, Object> user = createUser(firstnameStr, lastnameStr, usernameStr, passwordStr, vehicleType);

                    if (vehicleType != null){
                        addUserToDatabase(user);
                        navigateToLogin();
                    }

                    else{
                        Toast.makeText(getContext(), "Please select a vehicle type", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    private String getVehicleType() {
        int selectedId = vehicle_type.getCheckedRadioButtonId();
        if (selectedId == kalesaRadioButton.getId()) {
            return "kalesa";
        } else if (selectedId == pedicabRadioButton.getId()) {
            return "pedicab";
        } else if (selectedId == tricycleRadioButton.getId()) {
            return "tricycle";
        }

        return null;
    }
}