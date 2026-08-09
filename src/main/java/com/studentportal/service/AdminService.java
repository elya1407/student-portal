package com.studentportal.service;

import com.studentportal.model.Admin;
import com.studentportal.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Администратор не найден"));
    }
}
