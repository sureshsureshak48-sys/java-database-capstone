package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 1. Return available time slots for a doctor on a given date
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Doctor doctor = doctorOpt.get();
        List<String> allSlots = new ArrayList<>(doctor.getAvailableTimes());

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Set<String> bookedTimes = bookedAppointments.stream()
                .map(appt -> appt.getAppointmentTime().toLocalTime().format(timeFormatter))
                .collect(Collectors.toSet());

        return allSlots.stream()
                .filter(slot -> bookedTimes.stream().noneMatch(slot::startsWith))
                .collect(Collectors.toList());
    }

    // 2. Validate doctor login credentials and return structured response
    @Transactional(readOnly = true)
    public ResponseEntity> validateDoctor(Doctor loginDetails) {
        Map<String, String> response = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(loginDetails.getEmail());

        if (doctor == null) {
            response.put("message", "Doctor not found with provided email");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (!doctor.getPassword().equals(loginDetails.getPassword())) {
            response.put("message", "Invalid credentials");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.generateToken(doctor.getEmail());
        response.put("token", token);
        response.put("message", "Login successful");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 3. Save a new doctor
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1; // Already exists
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 4. Retrieve all doctors
    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }
}
