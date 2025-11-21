package com.project.back_end.controllers;

import com.project.back_end.models.Admin;
import com.project.back_end.services.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "admin")
public class AdminController {

    // 2. Autowire Service Dependency:
    private final ValidationService validationService;

    @Autowired
    public AdminController(ValidationService validationService) {
        this.validationService = validationService;
    }

    // 3. Define the `adminLogin` Method:
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        // Delegate authentication logic to the validateAdmin method in the service layer
        return validationService.validateAdmin(admin);
    }
}