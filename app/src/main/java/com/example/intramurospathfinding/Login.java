package com.example.intramurospathfinding;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Login extends Fragment {


    View v;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Login() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Login.
     */
    // TODO: Rename and change types and number of parameters
    public static Login newInstance(String param1, String param2) {
        Login fragment = new Login();
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



    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText email, password;
    Button loginBtn;
    TextView linkSignin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_login, container, false);

        email = (EditText)  v.findViewById(R.id.loginEmailField);
        password = (EditText)  v.findViewById(R.id.loginPasswordField);
        loginBtn = (Button) v.findViewById(R.id.loginBtn);
        linkSignin = (TextView) v.findViewById(R.id.linkSignin);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailStr = email.getText().toString();
                String passwordStr = password.getText().toString();

                db.collection("users")
                        .whereEqualTo("username", emailStr)
                        .whereEqualTo("password", passwordStr)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        Toast.makeText(getContext(), "Login Success", Toast.LENGTH_SHORT).show();
                                        // Navigate to home
                                        CurrentUser.firstname = task.getResult().getDocuments().get(0).get("firstname").toString();
                                        CurrentUser.lastname = task.getResult().getDocuments().get(0).get("lastname").toString();
                                        CurrentUser.email = task.getResult().getDocuments().get(0).get("username").toString();
                                        CurrentUser.user_id = task.getResult().getDocuments().get(0).getId();
                                        CurrentUser.vehicle_type = task.getResult().getDocuments().get(0).get("vehicle_type").toString();
                                        Fragment selectedFragment = new Home();
                                        if (selectedFragment != null) {
                                            getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container, selectedFragment).commit();
                                            getActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
                                        }


                                    } else {
                                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Query failed
                                }
                            }
                        });
            }
        });

        linkSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment selectedFragment = new Registration();
                if (selectedFragment != null) {
                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container, selectedFragment).commit();
                    getActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);

                }
            }
        });



        return v;
    }
}