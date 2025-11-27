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

    // ADD THIS METHOD - Root mapping for "/"
    @GetMapping("/")
    public String index() {
        return "redirect:/index.html"; // This will look for index.html in templates folder
    }

    
    /**
     * Handles HTTP GET requests to /adminDashboard/{token}
     * Validates admin token and returns appropriate view or redirect
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "admin");
        
        if (validationResponse.getStatusCode() == HttpStatus.OK && 
            validationResponse.getBody() != null && 
            !validationResponse.getBody().containsKey("error")) {
            return "admin/adminDashboard";
        } else {
            return "redirect:/";
        }
    }

    /**
     * Handles HTTP GET requests to /doctorDashboard/{token}
     * Validates doctor token and returns appropriate view or redirect
     */
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "doctor");
        
        if (validationResponse.getStatusCode() == HttpStatus.OK && 
            validationResponse.getBody() != null && 
            !validationResponse.getBody().containsKey("error")) {
            return "doctor/doctorDashboard";
        } else {
            return "redirect:/";
        }
    }

    /**
     * Handles HTTP GET requests to /patientDashboard/{token}
     * Validates patient token and returns appropriate view or redirect
     */
    @GetMapping("/patientDashboard/{token}")
    public String patientDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, String>> validationResponse = service.validateToken(token, "patient");
        
        if (validationResponse.getStatusCode() == HttpStatus.OK && 
            validationResponse.getBody() != null && 
            !validationResponse.getBody().containsKey("error")) {
            return "patient/patientDashboard";
        } else {
            return "redirect:/";
        }
    }

    /**
     * Generic dashboard handler that determines role from token
     */
    @GetMapping("/dashboard/{token}")
    public String dashboard(@PathVariable String token) {
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

        return "redirect:/";
    }

    /**
     * Login page - you can remove this if you use the index() method above
     */
    @GetMapping("/login")
    public String login() {        
        return "redirect:/index.html";
    }

    /**
     * Home page redirect
     */
    @GetMapping("/home")
    public String home() {
        return "redirect:/";
    }
    // Public access endpoints for role selection
    @GetMapping("/adminAccess")
    public String adminAccess() {
        return "admin/adminDashboard";
    }

    @GetMapping("/doctorAccess") 
    public String doctorAccess() {
        return "doctor/doctorDashboard";
    }
     
}