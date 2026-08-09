package com.studentportal.repository;

import com.studentportal.model.Excuse;
import com.studentportal.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExcuseRepository extends JpaRepository<Excuse, Long> {
    List<Excuse> findByStudentOrderByDateFromDesc(Student student);
    List<Excuse> findAllByOrderByCreatedAtDesc();
}
