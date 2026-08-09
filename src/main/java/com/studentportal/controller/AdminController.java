package com.studentportal.controller;

import com.studentportal.model.Attendance;
import com.studentportal.model.Student;
import com.studentportal.service.AppSettingsService;
import com.studentportal.service.AttendanceService;
import com.studentportal.service.ExcuseService;
import com.studentportal.service.PostService;
import com.studentportal.service.ScheduleService;
import com.studentportal.service.StudentService;
import com.studentportal.util.ExcelExporter;
import com.studentportal.util.WeekTypeUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final ScheduleService scheduleService;
    private final PostService postService;
    private final ExcelExporter excelExporter;
    private final AppSettingsService appSettingsService;
    private final ExcuseService excuseService;

    public AdminController(StudentService studentService,
                           AttendanceService attendanceService,
                           ScheduleService scheduleService,
                           PostService postService,
                           ExcelExporter excelExporter,
                           AppSettingsService appSettingsService,
                           ExcuseService excuseService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.scheduleService = scheduleService;
        this.postService = postService;
        this.excelExporter = excelExporter;
        this.appSettingsService = appSettingsService;
        this.excuseService = excuseService;
    }

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("todayMarks", attendanceService.todayMarks());
        model.addAttribute("activeWindows", attendanceService.activeWindows());
        model.addAttribute("notMarked", attendanceService.notMarkedTodayBySubgroup());
        model.addAttribute("currentWeekType", WeekTypeUtil.getWeekType(LocalDate.now()));
        model.addAttribute("anchorDate", appSettingsService.getAnchorDate());
        model.addAttribute("anchorType", appSettingsService.getAnchorType());
        model.addAttribute("todayIso", LocalDate.now().toString());
        return "admin/dashboard";
    }

    @PostMapping("/settings/week")
    public String setWeekAnchor(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 @RequestParam String weekType,
                                 RedirectAttributes redirectAttributes) {
        try {
            appSettingsService.setAnchor(date, weekType);
            redirectAttributes.addFlashAttribute("success",
                    "Готово: на " + date + " установлена неделя «" + weekType + "». Дальше будет чередоваться автоматически.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @GetMapping("/students")
    public String students(@RequestParam(required = false) Integer subgroup, Model model) {
        model.addAttribute("students", studentService.findBySubgroup(subgroup));
        model.addAttribute("selectedSubgroup", subgroup);
        return "admin/students";
    }

    @PostMapping("/students/{id}/update")
    public String updateStudent(@PathVariable Long id,
                                @RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam String email,
                                @RequestParam Integer subgroup,
                                @RequestParam(required = false) String recordBookNumber,
                                RedirectAttributes redirectAttributes) {
        try {
            studentService.adminUpdate(id, fullName, phone, email, subgroup, recordBookNumber);
            redirectAttributes.addFlashAttribute("success", "Данные студента обновлены");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/create")
    public String createStudent(@RequestParam String fullName,
                                @RequestParam String recordBookNumber,
                                @RequestParam Integer subgroup,
                                RedirectAttributes redirectAttributes) {
        try {
            studentService.adminCreate(fullName, recordBookNumber, subgroup);
            redirectAttributes.addFlashAttribute("success", "Студент добавлен. Он сможет зарегистрироваться по ФИО и зачётке " + recordBookNumber);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            String newPassword = studentService.adminResetPassword(id);
            redirectAttributes.addFlashAttribute("success",
                    "Новый пароль: " + newPassword + " — сообщите его студенту, он сможет сразу войти с ним по своей зачётке.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @PostMapping("/students/{id}/active")
    public String activeStudent(@PathVariable Long id, @RequestParam boolean active, RedirectAttributes redirectAttributes) {
        studentService.setActive(id, active);
        redirectAttributes.addFlashAttribute("success", active ? "Студент активирован" : "Студент деактивирован");
        return "redirect:/admin/students";
    }

    @GetMapping("/attendance")
    public String attendance(@RequestParam(required = false) String recordBook,
                             Model model) {
        model.addAttribute("todayMarks", attendanceService.todayMarks());
        model.addAttribute("notMarked", attendanceService.notMarkedTodayBySubgroup());
        model.addAttribute("activeWindows", attendanceService.activeWindows());
        model.addAttribute("allStudents", studentService.findAll());
        if (recordBook != null && !recordBook.isBlank()) {
            try {
                var student = studentService.findByRecordBook(recordBook);
                model.addAttribute("foundStudent", student);
                model.addAttribute("studentHistory", attendanceService.history(student));
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "admin/attendance";
    }

    @PostMapping("/attendance/mark-manual")
    public String markManual(@RequestParam Long studentId,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.findAll().stream()
                    .filter(s -> s.getId().equals(studentId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));
            LocalDate markDate = date != null ? date : LocalDate.now();
            attendanceService.adminMark(student, markDate, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Отметка добавлена вручную: " + student.getFullName() + " — " + markDate);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/attendance";
    }

    @PostMapping("/attendance/open")
    public String openAttendance(@RequestParam Integer subgroup,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            attendanceService.openWindow(subgroup, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Отметка открыта на 15 минут для подгруппы " + subgroup);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/attendance";
    }

    @PostMapping("/attendance/close/{id}")
    public String closeAttendance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.closeWindow(id);
            redirectAttributes.addFlashAttribute("success", "Отметка закрыта");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/attendance";
    }

    @GetMapping("/schedule")
    public String schedule(Model model) {
        model.addAttribute("items", scheduleService.findAll());
        return "admin/schedule";
    }

    @PostMapping("/schedule")
    public String createSchedule(@RequestParam String weekType,
                                  @RequestParam String dayOfWeek,
                                  @RequestParam Integer subgroup,
                                  @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                                  @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
                                  @RequestParam String discipline,
                                  RedirectAttributes redirectAttributes) {
        try {
            scheduleService.create(weekType, dayOfWeek, subgroup, startTime, endTime, discipline);
            redirectAttributes.addFlashAttribute("success", "Пара добавлена в расписание");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/schedule";
    }

    @PostMapping("/schedule/{id}/update")
    public String updateSchedule(@PathVariable Long id,
                                  @RequestParam String weekType,
                                  @RequestParam String dayOfWeek,
                                  @RequestParam Integer subgroup,
                                  @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                                  @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
                                  @RequestParam String discipline,
                                  RedirectAttributes redirectAttributes) {
        try {
            scheduleService.update(id, weekType, dayOfWeek, subgroup, startTime, endTime, discipline);
            redirectAttributes.addFlashAttribute("success", "Пара обновлена");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/schedule";
    }

    @PostMapping("/schedule/{id}/delete")
    public String deleteSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        scheduleService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Пара удалена из расписания");
        return "redirect:/admin/schedule";
    }

    @GetMapping("/excuses")
    public String excuses(Model model) {
        model.addAttribute("excuses", excuseService.all());
        return "admin/excuses";
    }

    @GetMapping("/posts")
    public String posts(Model model) {
        model.addAttribute("posts", postService.findAll());
        return "admin/posts";
    }

    @PostMapping("/posts")
    public String createPost(@RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(required = false) Integer forSubgroup,
                             @RequestParam(required = false) MultipartFile file,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            postService.create(title, content, forSubgroup, file, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Пост опубликован");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{id}/update")
    public String updatePost(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String content,
                             @RequestParam(required = false) Integer forSubgroup,
                             RedirectAttributes redirectAttributes) {
        postService.update(id, title, content, forSubgroup);
        redirectAttributes.addFlashAttribute("success", "Пост обновлён");
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        postService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Пост удалён");
        return "redirect:/admin/posts";
    }

    @GetMapping("/export/students")
    public ResponseEntity<byte[]> exportStudents() {
        byte[] bytes = excelExporter.exportStudents(studentService.findAll());
        return excel("students_603-41.xlsx", bytes);
    }

    @GetMapping("/export/attendance")
    public ResponseEntity<byte[]> exportAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.withDayOfMonth(1) : from;
        byte[] bytes = excelExporter.exportAttendance(attendanceService.between(start, end));
        return excel("attendance_603-41.xlsx", bytes);
    }

    private ResponseEntity<byte[]> excel(String fileName, byte[] bytes) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
