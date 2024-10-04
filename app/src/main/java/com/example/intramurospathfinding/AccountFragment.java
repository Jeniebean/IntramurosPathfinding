package com.example.intramurospathfinding;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
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

    Button updateAccountButton, logoutButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        EditText firstname = v.findViewById(R.id.accountFirstName);
        EditText lastname = v.findViewById(R.id.accountLastName);
        EditText username = v.findViewById(R.id.accountEmail);
        updateAccountButton = v.findViewById(R.id.updateAccountButton);
        logoutButton = v.findViewById(R.id.logoutButton);

        firstname.setText(CurrentUser.firstname);
        lastname.setText(CurrentUser.lastname);
        username.setText(CurrentUser.email);

        updateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentUser.firstname = firstname.getText().toString();
                CurrentUser.lastname = lastname.getText().toString();
                CurrentUser.email = username.getText().toString();

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("users").document(CurrentUser.user_id).update("firstname", CurrentUser.firstname, "lastname", CurrentUser.lastname, "username", CurrentUser.email).addOnCompleteListener(
                        task -> {
                            Toast.makeText(getContext(), "Account updated", Toast.LENGTH_SHORT).show();
                        }
                );

            }
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentUser.firstname = "";
                CurrentUser.lastname = "";
                CurrentUser.email = "";
                CurrentUser.user_id = "";
                CurrentUser.vehicle_type = "";
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Login()).commit();
            }
        });


        return v;
    }
}