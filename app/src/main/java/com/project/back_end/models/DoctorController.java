package com.project.back_end.controllers;

import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    @Autowired
    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    // 1. Get Doctor Availability (with dynamic parameters & token validation)
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();

        // Validate token
        ResponseEntity> tokenValidation = service.validateToken(token, user);
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            response.put("message", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, date);
        response.put("doctorId", doctorId);
        response.put("date", date.toString());
        response.put("availableTimes", availableSlots);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 2. Get All Doctors
    @GetMapping
    public ResponseEntity> getDoctors() {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorService.getDoctors();
        response.put("doctors", doctors);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 3. Add a New Doctor (Admin only)
    @PostMapping("/{token}")
    public ResponseEntity> saveDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {

        Map<String, String> response = new HashMap<>();
        ResponseEntity> tokenValidation = service.validateToken(token, "admin");
        if (!tokenValidation.getStatusCode().is2xxSuccessful()) {
            response.put("message", "Unauthorized access");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.saveDoctor(doctor);
        if (result == 1) {
            response.put("message", "Doctor added to db");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else if (result == -1) {
            response.put("message", "Doctor already exists");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } else {
            response.put("message", "Some internal error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4. Doctor Login
    @PostMapping("/login")
    public ResponseEntity> doctorLogin(@RequestBody Doctor loginDetails) {
        return doctorService.validateDoctor(loginDetails);
    }

    // 5. Filter Doctors by Speciality and Time
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {
        return ResponseEntity.ok(service.filterDoctor(name, speciality, time));
    }
}
