package com.example.intramurospathfinding;

import android.view.View;
import android.widget.EditText;
import android.widget.Button;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LoginTest {


    private Login loginFragment;

    @Before
    public void setUp() {
       loginFragment = new Login();

    }

@Test
public void testVerifyLogin() {
    // Arrange
    String testEmail = "1";
    String testPassword = "1";



  FirebaseFirestore db = mock(FirebaseFirestore.class);
}}