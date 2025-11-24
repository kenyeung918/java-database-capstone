package com.project.back_end.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.Service;

import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private Service service;

    /**
     * Handles HTTP GET requests to /adminDashboard/{token}
     * Validates admin token and returns appropriate view or redirect
     * 
     * @param token The access token from the URL path
     * @return admin dashboard view if token is valid, redirect to login if invalid
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        // FIXED: Handle ResponseEntity properly
        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "admin");
        
        // If the token is valid (status code is OK and no errors in response)
        if (validationResponse.getStatusCode() == HttpStatus.OK && 
            validationResponse.getBody() != null && 
            !validationResponse.getBody().containsKey("error")) {
            // Forward the user to the "admin/adminDashboard" view
            return "admin/adminDashboard";
        } else {
            // If invalid, redirects to the root URL, likely the login or home page
            return "redirect:/";
        }
    }

    /**
     * Handles HTTP GET requests to /doctorDashboard/{token}
     * Validates doctor token and returns appropriate view or redirect
     * 
     * @param token The access token from the URL path
     * @return doctor dashboard view if token is valid, redirect to login if invalid
     */
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        // FIXED: Handle ResponseEntity properly
        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "doctor");
        
        // If the token is valid
        if (validationResponse.getStatusCode() == HttpStatus.OK && 
            validationResponse.getBody() != null && 
            !validationResponse.getBody().containsKey("error")) {
            // Forward the user to the "doctor/doctorDashboard" view
            return "doctor/doctorDashboard";
        } else {
            // If the token is invalid, redirect to the root URL
            return "redirect:/";
        }
    }

    /**
     * Handles HTTP GET requests to /patientDashboard/{token}
     * Validates patient token and returns appropriate view or redirect
     * 
     * @param token The access token from the URL path
     * @return patient dashboard view if token is valid, redirect to login if invalid
     */
    @GetMapping("/patientDashboard/{token}")
    public String patientDashboard(@PathVariable String token) {
        // FIXED: Handle ResponseEntity properly
        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        
        // If the token is valid
        if (validationResponse.getStatusCode() == HttpStatus.OK && 
            validationResponse.getBody() != null && 
            !validationResponse.getBody().containsKey("error")) {
            // Forward the user to the "patient/patientDashboard" view
            return "patient/patientDashboard";
        } else {
            // If the token is invalid, redirect to the root URL
            return "redirect:/";
        }
    }

    /**
     * Generic dashboard handler that determines role from token
     */
    @GetMapping("/dashboard/{token}")
    public String dashboard(@PathVariable String token) {
        // Try to validate for different roles
        ResponseEntity<Map<String, String>> adminResponse = service.validateToken(token, "admin");
        if (adminResponse.getStatusCode() == HttpStatus.OK && 
            adminResponse.getBody() != null && 
            !adminResponse.getBody().containsKey("error")) {
            return "admin/adminDashboard";
        }

        ResponseEntity<Map<String, String>> doctorResponse = service.validateToken(token, "doctor");
        if (doctorResponse.getStatusCode() == HttpStatus.OK && 
            doctorResponse.getBody() != null && 
            !doctorResponse.getBody().containsKey("error")) {
            return "doctor/doctorDashboard";
        }

        ResponseEntity<Map<String, String>> patientResponse = service.validateToken(token, "patient");
        if (patientResponse.getStatusCode() == HttpStatus.OK && 
            patientResponse.getBody() != null && 
            !patientResponse.getBody().containsKey("error")) {
            return "patient/patientDashboard";
        }

        // If no valid role found, redirect to login
        return "redirect:/";
    }

    /**
     * Login page
     */
    @GetMapping("/")
    public String login() {
        return "login";
    }

    /**
     * Home page redirect
     */
    @GetMapping("/home")
    public String home() {
        return "redirect:/";
    }
}