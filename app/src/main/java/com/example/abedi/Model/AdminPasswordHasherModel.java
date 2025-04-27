package com.example.abedi.Model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AdminPasswordHasherModel {

    // Hashes the password using SHA-256 algorithm
    public static String hashPassword(String password) {
        try {
            // Create a MessageDigest instance for SHA-256 hashing algorithm
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Ig coconvert niya yung password string into bytes, tapos ig hahash niya
            byte[] hashedBytes = md.digest(password.getBytes());

            // Create a StringBuilder to store the final hashed string
            StringBuilder sb = new StringBuilder();

            // Loop through the hashed bytes and convert each byte to a two-digit hexadecimal string
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b)); // So ig fuformat niya yung byte as a 2-digit hexadecimal value
            }

            //then ig rereturn niya yung final hashed password in string
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

            return null;
        }
    }
}


