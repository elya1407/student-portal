package com.studentportal.service;

import com.studentportal.model.Schedule;
import com.studentportal.model.Student;
import com.studentportal.repository.ScheduleRepository;
import com.studentportal.util.WeekTypeUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public Optional<Schedule> findTodayForStudent(Student student) {
        LocalDate today = LocalDate.now();
        return scheduleRepository.findByWeekTypeAndDayOfWeekAndSubgroup(
                WeekTypeUtil.getWeekType(today),
                WeekTypeUtil.getDayCode(today),
                student.getSubgroup()
        );
    }

    public Optional<Schedule> findTodayForSubgroup(Integer subgroup) {
        LocalDate today = LocalDate.now();
        return scheduleRepository.findByWeekTypeAndDayOfWeekAndSubgroup(
                WeekTypeUtil.getWeekType(today),
                WeekTypeUtil.getDayCode(today),
                subgroup
        );
    }

    public List<Schedule> findForStudent(Student student) {
        return scheduleRepository.findBySubgroupOrderByWeekTypeAscDayOfWeekAscStartTimeAsc(student.getSubgroup()).stream()
                .sorted(Comparator.comparing(Schedule::getWeekType)
                        .thenComparing(s -> WeekTypeUtil.dayOrder(s.getDayOfWeek()))
                        .thenComparing(Schedule::getStartTime))
                .toList();
    }

    public List<Schedule> findForSubgroupAndWeek(Integer subgroup, String weekType) {
        return scheduleRepository.findByWeekTypeAndSubgroupOrderByDayOfWeekAscStartTimeAsc(weekType, subgroup).stream()
                .sorted(Comparator.comparing((Schedule s) -> WeekTypeUtil.dayOrder(s.getDayOfWeek()))
                        .thenComparing(Schedule::getStartTime))
                .toList();
    }

    /** Все пары для управления в админке, отсортированные для удобного просмотра. */
    public List<Schedule> findAll() {
        return scheduleRepository.findAll().stream()
                .sorted(Comparator.comparing(Schedule::getSubgroup)
                        .thenComparing(Schedule::getWeekType)
                        .thenComparing((Schedule s) -> WeekTypeUtil.dayOrder(s.getDayOfWeek()))
                        .thenComparing(Schedule::getStartTime))
                .toList();
    }

    @jakarta.transaction.Transactional
    public Schedule create(String weekType, String dayOfWeek, Integer subgroup,
                            java.time.LocalTime startTime, java.time.LocalTime endTime, String discipline) {
        scheduleRepository.findByWeekTypeAndDayOfWeekAndSubgroup(weekType, dayOfWeek, subgroup)
                .ifPresent(s -> {
                    throw new IllegalArgumentException(
                            "У подгруппы " + subgroup + " уже есть пара в " + dayOfWeek + " (" + weekType + "). " +
                                    "Отредактируйте существующую запись или удалите её.");
                });
        Schedule schedule = new Schedule();
        schedule.setWeekType(weekType);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setSubgroup(subgroup);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setDiscipline(discipline);
        return scheduleRepository.save(schedule);
    }

    @jakarta.transaction.Transactional
    public Schedule update(Long id, String weekType, String dayOfWeek, Integer subgroup,
                            java.time.LocalTime startTime, java.time.LocalTime endTime, String discipline) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пара не найдена"));

        scheduleRepository.findByWeekTypeAndDayOfWeekAndSubgroup(weekType, dayOfWeek, subgroup)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new IllegalArgumentException(
                            "У подгруппы " + subgroup + " уже есть другая пара в " + dayOfWeek + " (" + weekType + ")");
                });

        schedule.setWeekType(weekType);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setSubgroup(subgroup);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setDiscipline(discipline);
        return scheduleRepository.save(schedule);
    }

    @jakarta.transaction.Transactional
    public void delete(Long id) {
        scheduleRepository.deleteById(id);
    }
}
