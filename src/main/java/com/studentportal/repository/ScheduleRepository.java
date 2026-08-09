package com.studentportal.repository;

import com.studentportal.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    Optional<Schedule> findByWeekTypeAndDayOfWeekAndSubgroup(String weekType, String dayOfWeek, Integer subgroup);
    List<Schedule> findByWeekTypeAndSubgroupOrderByDayOfWeekAscStartTimeAsc(String weekType, Integer subgroup);
    List<Schedule> findBySubgroupOrderByWeekTypeAscDayOfWeekAscStartTimeAsc(Integer subgroup);
}
