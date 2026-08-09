package com.studentportal.repository;

import com.studentportal.model.AttendanceWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceWindowRepository extends JpaRepository<AttendanceWindow, Long> {
    @Query("select w from AttendanceWindow w where w.active = true and w.subgroup = :subgroup and w.weekType = :weekType and w.dayOfWeek = :dayOfWeek and w.openedAt <= :now and w.closesAt >= :now order by w.openedAt desc")
    List<AttendanceWindow> findActiveWindows(@Param("subgroup") Integer subgroup,
                                             @Param("weekType") String weekType,
                                             @Param("dayOfWeek") String dayOfWeek,
                                             @Param("now") LocalDateTime now);

    List<AttendanceWindow> findByActiveTrueOrderByOpenedAtDesc();

    default Optional<AttendanceWindow> findFirstActive(Integer subgroup, String weekType, String dayOfWeek, LocalDateTime now) {
        return findActiveWindows(subgroup, weekType, dayOfWeek, now).stream().findFirst();
    }
}
