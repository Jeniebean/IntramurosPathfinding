package com.example.intramurospathfinding;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testKalesaFare(){

        double BASE_RATE = 1000;
        double PER_MINUTE_RATE = BASE_RATE / 60;
        CurrentUser.vehicle_type = "kalesa";
        double fare = HistoryAdapter.calculateFare(BASE_RATE, PER_MINUTE_RATE,"1", "1.0", 90);
        assertEquals(1500, fare, 0.0);
        System.out.println("Fare Test Passed");
    }


    @Test
    public void computeBaseFare_regularKalesa() {
        Double[] expected = {1000.0, 1000.0 / 60};
        CurrentUser.vehicle_type = "kalesa";
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("kalesa", "regular"));
    }

    @Test
    public void computeBaseFare_discountedKalesa() {
        Double[] expected = {800.0, 800.0 / 60};
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("kalesa", "student"));
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("kalesa", "senior"));
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("kalesa", "pwd"));
    }

    @Test
    public void computeBaseFare_regularPedicab() {
        Double[] expected = {400.0, 400.0 / 60};
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("pedicab", "regular"));
    }

    @Test
    public void computeBaseFare_discountedPedicab() {
        Double[] expected = {320.0, 320.0 / 60};
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("pedicab", "student"));
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("pedicab", "senior"));
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("pedicab", "pwd"));
    }

    @Test
    public void computeBaseFare_regularTricycle() {
        Double[] expected = {200.0, 200.0 / 30};
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("tricycle", "regular"));
    }

    @Test
    public void computeBaseFare_discountedTricycle() {
        Double[] expected = {120.0, 120.0 / 30};
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("tricycle", "student"));
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("tricycle", "senior"));
        assertArrayEquals(expected, HistoryAdapter.computeBaseFare("tricycle", "pwd"));
    }

    @Test
    public void validateInput_validInput_returnsTrue() {
        assertTrue(Registration.validateInput("John", "Doe", "password123"));
    }

    @Test
    public void validateInput_invalidFirstName_returnsFalse() {
        assertFalse(Registration.validateInput("John1", "Doe", "password123"));
    }

    @Test
    public void validateInput_invalidLastName_returnsFalse() {
        assertFalse(Registration.validateInput("John", "Doe1", "password123"));
    }

    @Test
    public void validateInput_invalidPassword_returnsFalse() {
        assertFalse(Registration.validateInput("John", "Doe", "p".repeat(256)));
    }

    @Test
    public void isNameValid_validName_returnsTrue() {
        assertTrue(Registration.isNameValid("John"));
    }

    @Test
    public void isNameValid_invalidName_returnsFalse() {
        assertFalse(Registration.isNameValid("John1"));
    }

    @Test
    public void isPasswordValid_validPassword_returnsTrue() {
        assertTrue(Registration.isPasswordValid("password123"));
    }

    @Test
    public void isPasswordValid_invalidPassword_returnsFalse() {
        assertFalse(Registration.isPasswordValid("p".repeat(256)));
    }

    @Test
    public void computeFare_regularKalesa() {
        CurrentUser.vehicle_type = "kalesa";
        Double[] baseFare = HistoryAdapter.computeBaseFare("kalesa", "regular");
        double fare = HistoryAdapter.calculateFare(baseFare[0], baseFare[1], "1", "1", 90);
        assertEquals(1500.0, fare, 0.01);
    }

    @Test
    public void computeFare_discountedKalesa() {
        CurrentUser.vehicle_type = "kalesa";
        Double[] baseFare = HistoryAdapter.computeBaseFare("kalesa", "student");
        double fare = HistoryAdapter.calculateFare(baseFare[0], baseFare[1], "1", "1", 90);
        assertEquals(1200.0, fare, 0.01);
    }


    @Test
    public void computeFare_regularPedicab() {
        CurrentUser.vehicle_type = "pedicab";
        Double[] baseFare = HistoryAdapter.computeBaseFare("pedicab", "regular");
        double fare = HistoryAdapter.calculateFare(baseFare[0], baseFare[1], "1", "1", 90);
        assertEquals(600, fare, 0.01);
    }

    @Test
    public void computeFare_discountedPedicab() {
        CurrentUser.vehicle_type = "pedicab";
        Double[] baseFare = HistoryAdapter.computeBaseFare("pedicab", "student");
        double fare = HistoryAdapter.calculateFare(baseFare[0], baseFare[1], "1", "1", 90);
        assertEquals(480, fare, 0.01);
    }

    @Test
    public void computeFare_regularTricycle() {
        CurrentUser.vehicle_type = "tricycle";
        Double[] baseFare = HistoryAdapter.computeBaseFare("tricycle", "regular");
        double fare = HistoryAdapter.calculateFare(baseFare[0], baseFare[1], "1", "1", 90);
        assertEquals(600, fare, 0.01);
    }

    @Test
    public void computeFare_discountedTricycle() {
        CurrentUser.vehicle_type = "tricycle";
        Double[] baseFare = HistoryAdapter.computeBaseFare("tricycle", "student");
        double fare = HistoryAdapter.calculateFare(baseFare[0], baseFare[1], "1", "1", 90);
        assertEquals(360, fare, 0.01);
    }




}