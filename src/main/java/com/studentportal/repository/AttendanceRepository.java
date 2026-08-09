package com.studentportal.repository;

import com.studentportal.model.Attendance;
import com.studentportal.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentAndDate(Student student, LocalDate date);
    boolean existsByStudentAndDate(Student student, LocalDate date);
    List<Attendance> findByDateOrderBySubgroupAscTimeAsc(LocalDate date);
    List<Attendance> findByStudentOrderByDateDescTimeDesc(Student student);
    long countByStudent(Student student);

    @Query("select a from Attendance a where a.date between :from and :to order by a.date asc, a.subgroup asc, a.student.fullName asc")
    List<Attendance> findBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
