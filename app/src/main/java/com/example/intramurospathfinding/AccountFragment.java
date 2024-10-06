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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

        updateAccountButton = v.findViewById(R.id.updateAccountButton);
        logoutButton = v.findViewById(R.id.logoutButton);

        setupUpdateAccountButton(inflater);
        setupLogoutButton();

        return v;
    }

    private void setupUpdateAccountButton(LayoutInflater inflater) {
        updateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                builder.setTitle("Update Account");
                View view = inflater.inflate(R.layout.update_account_dialog, null);
                builder.setView(view);
                EditText firstnameEditText = view.findViewById(R.id.firstnameEditText);
                EditText lastnameEditText = view.findViewById(R.id.lastnameEditText);
                EditText emailEditText = view.findViewById(R.id.emailEditText);
                EditText passwordEditText = view.findViewById(R.id.passwordEditText);
                EditText confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);

                firstnameEditText.setText(CurrentUser.firstname);
                lastnameEditText.setText(CurrentUser.lastname);
                emailEditText.setText(CurrentUser.email);
                builder.setPositiveButton("Update", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();


                    String firstname = firstnameEditText.getText().toString();
                    String lastname = lastnameEditText.getText().toString();
                    String email = emailEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    String confirmPassword = confirmPasswordEditText.getText().toString();
                    if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill up all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!password.equals(confirmPassword)) {
                        Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else if (!password.isEmpty() && !confirmPassword.isEmpty()) {
                        db.collection("users").document(CurrentUser.user_id).update("firstname", firstname, "lastname", lastname, "email", email, "password", password).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                CurrentUser.firstname = firstname;
                                CurrentUser.lastname = lastname;
                                CurrentUser.email = email;
                                Toast.makeText(getContext(), "Account updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to update account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        db.collection("users").document(CurrentUser.user_id).update("firstname", firstname, "lastname", lastname, "email", email).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                CurrentUser.firstname = firstname;
                                CurrentUser.lastname = lastname;
                                CurrentUser.email = email;
                                Toast.makeText(getContext(), "Account updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to update account", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.show();
            }
        });
    }

    private void setupLogoutButton() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentUser.firstname = "";
                CurrentUser.lastname = "";
                CurrentUser.email = "";
                CurrentUser.user_id = "";
                CurrentUser.vehicle_type = "";
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Login()).commit();
                getActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
            }
        });
    }
}