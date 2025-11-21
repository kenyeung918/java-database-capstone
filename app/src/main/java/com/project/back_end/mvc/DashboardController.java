package com.project.back_end.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.Service;

import java.util.Map;

@Controller
public class DashboardController {

    // 2. Autowire the Shared Service:
    @Autowired
    private Service service;

    // 3. Define the `adminDashboard` Method:
    /**
     * Handles HTTP GET requests to /adminDashboard/{token}
     * Validates admin token and returns appropriate view or redirect
     * 
     * @param token The access token from the URL path
     * @return admin dashboard view if token is valid, redirect to login if invalid
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        // Validate the token using the shared service for the "admin" role
        Map<String, String> validationResult = service.validateToken(token, "admin");
        
        // If the token is valid (i.e., no errors returned)
        if (validationResult.isEmpty()) {
            // Forward the user to the "admin/adminDashboard" view
            return "admin/adminDashboard";
        } else {
            // If invalid, redirects to the root URL, likely the login or home page
            return "redirect:/";
        }
    }

    // 4. Define the `doctorDashboard` Method:
    /**
     * Handles HTTP GET requests to /doctorDashboard/{token}
     * Validates doctor token and returns appropriate view or redirect
     * 
     * @param token The access token from the URL path
     * @return doctor dashboard view if token is valid, redirect to login if invalid
     */
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        // Validate the token using the shared service for the "doctor" role
        Map<String, String> validationResult = service.validateToken(token, "doctor");
        
        // If the token is valid
        if (validationResult.isEmpty()) {
            // Forward the user to the "doctor/doctorDashboard" view
            return "doctor/doctorDashboard";
        } else {
            // If the token is invalid, redirect to the root URL
            return "redirect:/";
        }
    }
}