package com.example.intramurospathfinding;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {


    FirebaseFirestore db = FirebaseFirestore.getInstance();


    FragmentContainerView fragment_container;
    NavigationBarView bottomNavigationView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



        fragment_container = findViewById(R.id.fragment_container);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Splash()).commit();
        //fragment_container = findViewById(R.id.fragment_container);
        //getSupportFragmentManager()..addToBackStack(null().addToBackStack(null.replace(R.id.fragment_container, new Login()).commit();

        // Wait for 5 seconds then switch to the Login Screen fragment

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Login()).commit();
                    }
                },
                4000);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.INVISIBLE);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    selectedFragment = new Home();
                } else if (itemId == R.id.map) {
                    selectedFragment = new Maps();
                }
                else if (itemId == R.id.history) {
                    selectedFragment = new History();
                }
                else if (itemId == R.id.account) {
                    selectedFragment = new AccountFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container, selectedFragment).commit();
                }

                return true;
            }
        });

    }
}