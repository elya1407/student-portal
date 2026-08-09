package com.studentportal.service;

import com.studentportal.model.Admin;
import com.studentportal.model.Student;
import com.studentportal.repository.AdminRepository;
import com.studentportal.repository.StudentRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortalUserDetailsService implements UserDetailsService {
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

    public PortalUserDetailsService(StudentRepository studentRepository, AdminRepository adminRepository) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminRepository.findByUsername(username)
                .<UserDetails>map(this::adminDetails)
                .or(() -> studentRepository.findByRecordBookNumber(username).map(this::studentDetails))
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    private UserDetails adminDetails(Admin admin) {
        return new User(admin.getUsername(), admin.getPasswordHash(), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private UserDetails studentDetails(Student student) {
        if (!student.isRegistered()) {
            throw new UsernameNotFoundException("Студент ещё не зарегистрирован");
        }
        return new User(
                student.getRecordBookNumber(),
                student.getPasswordHash(),
                Boolean.TRUE.equals(student.getActive()),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }
}
