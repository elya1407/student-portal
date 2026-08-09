package com.studentportal.service;

import com.studentportal.dto.AttendanceStatsDto;
import com.studentportal.model.Attendance;
import com.studentportal.model.AttendanceWindow;
import com.studentportal.model.Schedule;
import com.studentportal.model.Student;
import com.studentportal.repository.AttendanceRepository;
import com.studentportal.repository.AttendanceWindowRepository;
import com.studentportal.repository.StudentRepository;
import com.studentportal.util.WeekTypeUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AttendanceWindowRepository windowRepository;
    private final ScheduleService scheduleService;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             AttendanceWindowRepository windowRepository,
                             ScheduleService scheduleService,
                             StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.windowRepository = windowRepository;
        this.scheduleService = scheduleService;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Attendance mark(Student student) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        if (!Boolean.TRUE.equals(student.getActive())) {
            throw new IllegalArgumentException("Учётная запись деактивирована");
        }
        if (attendanceRepository.existsByStudentAndDate(student, today)) {
            throw new IllegalArgumentException("Вы уже отметились сегодня");
        }

        Schedule schedule = scheduleService.findTodayForStudent(student)
                .orElseThrow(() -> new IllegalArgumentException("Сегодня для вашей подгруппы пары нет"));

        if (!canMarkNow(student, now)) {
            throw new IllegalArgumentException("Отметка закрыта. Кнопка активна 15 минут с начала пары или пока её открыл староста.");
        }

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setDate(today);
        attendance.setTime(now.toLocalTime().withNano(0));
        attendance.setWeekType(schedule.getWeekType());
        attendance.setSubgroup(student.getSubgroup());
        return attendanceRepository.save(attendance);
    }

    /**
     * Ручная отметка старостой — например, если студент присутствовал, но забыл нажать
     * "Я на паре". В отличие от самоотметки, не требует активного окна и не проверяет время.
     */
    @Transactional
    public Attendance adminMark(Student student, LocalDate date, String adminUsername) {
        if (attendanceRepository.existsByStudentAndDate(student, date)) {
            throw new IllegalArgumentException("На эту дату у студента уже есть отметка");
        }
        String weekType = WeekTypeUtil.getWeekType(date);
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setDate(date);
        attendance.setTime(LocalDateTime.now().toLocalTime().withNano(0));
        attendance.setWeekType(weekType);
        attendance.setSubgroup(student.getSubgroup());
        attendance.setMarkedBy(adminUsername);
        return attendanceRepository.save(attendance);
    }

    public boolean canMarkNow(Student student) {
        return canMarkNow(student, LocalDateTime.now());
    }

    public boolean canMarkNow(Student student, LocalDateTime now) {
        Optional<Schedule> scheduleOpt = scheduleService.findTodayForStudent(student);
        if (scheduleOpt.isEmpty()) return false;

        Schedule schedule = scheduleOpt.get();
        boolean byScheduleTime = !now.toLocalTime().isBefore(schedule.getStartTime())
                && now.toLocalTime().isBefore(schedule.getStartTime().plusMinutes(15));

        boolean byAdminWindow = windowRepository.findFirstActive(
                student.getSubgroup(),
                schedule.getWeekType(),
                schedule.getDayOfWeek(),
                now
        ).isPresent();

        return byScheduleTime || byAdminWindow;
    }

    public Optional<Attendance> findTodayMark(Student student) {
        return attendanceRepository.findByStudentAndDate(student, LocalDate.now());
    }

    public List<Attendance> history(Student student) {
        return attendanceRepository.findByStudentOrderByDateDescTimeDesc(student);
    }

    public AttendanceStatsDto stats(Student student) {
        long present = attendanceRepository.countByStudent(student);
        // Для прототипа считаем учебные дни с момента регистрации или с начала текущего месяца.
        LocalDate start = student.getRegisteredAt() == null
                ? LocalDate.now().withDayOfMonth(1)
                : student.getRegisteredAt().toLocalDate();
        long total = 0;
        LocalDate date = start;
        LocalDate today = LocalDate.now();
        while (!date.isAfter(today)) {
            String day = WeekTypeUtil.getDayCode(date);
            String week = WeekTypeUtil.getWeekType(date);
            boolean hasLesson = scheduleService.findForSubgroupAndWeek(student.getSubgroup(), week).stream()
                    .anyMatch(s -> s.getDayOfWeek().equals(day));
            if (hasLesson) total++;
            date = date.plusDays(1);
        }
        long missed = Math.max(total - present, 0);
        double percent = total == 0 ? 0 : Math.round((present * 10000.0 / total)) / 100.0;
        return new AttendanceStatsDto(total, present, missed, percent);
    }

    public Map<Integer, List<Student>> notMarkedTodayBySubgroup() {
        LocalDate today = LocalDate.now();
        Set<Long> markedIds = attendanceRepository.findByDateOrderBySubgroupAscTimeAsc(today).stream()
                .map(a -> a.getStudent().getId())
                .collect(Collectors.toSet());
        Map<Integer, List<Student>> result = new LinkedHashMap<>();
        for (int subgroup = 1; subgroup <= 3; subgroup++) {
            List<Student> notMarked = studentRepository.findBySubgroupAndActiveTrueOrderByFullName(subgroup).stream()
                    .filter(s -> !markedIds.contains(s.getId()))
                    .toList();
            result.put(subgroup, notMarked);
        }
        return result;
    }

    public List<Attendance> todayMarks() {
        return attendanceRepository.findByDateOrderBySubgroupAscTimeAsc(LocalDate.now());
    }

    public List<Attendance> between(LocalDate from, LocalDate to) {
        return attendanceRepository.findBetween(from, to);
    }

    @Transactional
    public AttendanceWindow openWindow(Integer subgroup, String adminUsername) {
        Schedule schedule = scheduleService.findTodayForSubgroup(subgroup)
                .orElseThrow(() -> new IllegalArgumentException("Сегодня у выбранной подгруппы нет пары"));
        LocalDateTime now = LocalDateTime.now();
        AttendanceWindow window = new AttendanceWindow();
        window.setSubgroup(subgroup);
        window.setWeekType(schedule.getWeekType());
        window.setDayOfWeek(schedule.getDayOfWeek());
        window.setSchedule(schedule);
        window.setOpenedAt(now.withNano(0));
        window.setClosesAt(now.plusMinutes(15).withNano(0));
        window.setActive(true);
        window.setOpenedBy(adminUsername);
        return windowRepository.save(window);
    }

    @Transactional
    public void closeWindow(Long id) {
        AttendanceWindow window = windowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Окно отметки не найдено"));
        window.setActive(false);
        windowRepository.save(window);
    }

    public List<AttendanceWindow> activeWindows() {
        return windowRepository.findByActiveTrueOrderByOpenedAtDesc().stream()
                .filter(w -> !w.getClosesAt().isBefore(LocalDateTime.now()))
                .toList();
    }
}
