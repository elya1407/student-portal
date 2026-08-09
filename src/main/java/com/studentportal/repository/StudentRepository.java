package com.studentportal.repository;

import com.studentportal.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRecordBookNumber(String recordBookNumber);
    List<Student> findBySubgroupOrderByFullName(Integer subgroup);
    List<Student> findBySubgroupAndActiveTrueOrderByFullName(Integer subgroup);
    List<Student> findByActiveTrueOrderBySubgroupAscFullNameAsc();
    List<Student> findByFullNameContainingIgnoreCaseOrderByFullName(String fullName);
}
