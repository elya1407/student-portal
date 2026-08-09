package com.studentportal.controller;

import com.studentportal.dto.PasswordChangeRequest;
import com.studentportal.dto.ProfileUpdateRequest;
import com.studentportal.model.Student;
import com.studentportal.service.AttendanceService;
import com.studentportal.service.ExcuseService;
import com.studentportal.service.ScheduleService;
import com.studentportal.service.StudentService;
import com.studentportal.util.WeekTypeUtil;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class StudentController {
    private final StudentService studentService;
    private final ScheduleService scheduleService;
    private final AttendanceService attendanceService;
    private final ExcuseService excuseService;

    public StudentController(StudentService studentService,
                             ScheduleService scheduleService,
                             AttendanceService attendanceService,
                             ExcuseService excuseService) {
        this.studentService = studentService;
        this.scheduleService = scheduleService;
        this.attendanceService = attendanceService;
        this.excuseService = excuseService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        model.addAttribute("student", student);
        model.addAttribute("weekType", WeekTypeUtil.getWeekType(LocalDate.now()).toUpperCase());
        model.addAttribute("todaySchedule", scheduleService.findTodayForStudent(student).orElse(null));
        model.addAttribute("canMark", attendanceService.canMarkNow(student));
        model.addAttribute("todayMark", attendanceService.findTodayMark(student).orElse(null));
        model.addAttribute("stats", attendanceService.stats(student));
        model.addAttribute("history", attendanceService.history(student).stream().limit(7).toList());
        return "dashboard";
    }

    @PostMapping("/attendance/mark")
    public String mark(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.mark(currentStudent(authentication));
            redirectAttributes.addFlashAttribute("success", "Отметка сохранена");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setPhone(student.getPhone());
        request.setEmail(student.getEmail());
        request.setBirthDate(student.getBirthDate());
        model.addAttribute("student", student);
        model.addAttribute("profileUpdateRequest", request);
        model.addAttribute("passwordChangeRequest", new PasswordChangeRequest());
        model.addAttribute("themeColors", java.util.List.of("peach", "lavender", "mint", "sky", "blush", "stone"));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @Valid @ModelAttribute ProfileUpdateRequest profileUpdateRequest,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        Student student = currentStudent(authentication);
        if (bindingResult.hasErrors()) {
            model.addAttribute("student", student);
            model.addAttribute("passwordChangeRequest", new PasswordChangeRequest());
            model.addAttribute("themeColors", java.util.List.of("peach", "lavender", "mint", "sky", "blush", "stone"));
            return "profile";
        }
        studentService.updateProfile(student, profileUpdateRequest);
        redirectAttributes.addFlashAttribute("success", "Профиль обновлён");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(Authentication authentication,
                                 @Valid @ModelAttribute PasswordChangeRequest passwordChangeRequest,
                                 RedirectAttributes redirectAttributes) {
        try {
            studentService.changePassword(currentStudent(authentication), passwordChangeRequest);
            redirectAttributes.addFlashAttribute("success", "Пароль изменён");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/schedule")
    public String schedule(Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        model.addAttribute("student", student);
        model.addAttribute("items", scheduleService.findForStudent(student));
        model.addAttribute("currentWeek", WeekTypeUtil.getWeekType(LocalDate.now()));
        return "schedule";
    }

    @GetMapping("/attendance")
    public String attendance(Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        model.addAttribute("student", student);
        model.addAttribute("history", attendanceService.history(student));
        model.addAttribute("stats", attendanceService.stats(student));
        return "attendance";
    }

    @PostMapping("/profile/avatar")
    public String uploadAvatar(Authentication authentication,
                               @RequestParam MultipartFile avatar,
                               RedirectAttributes redirectAttributes) {
        try {
            studentService.uploadAvatar(currentStudent(authentication), avatar);
            redirectAttributes.addFlashAttribute("success", "Аватар обновлён");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/theme")
    public String updateTheme(Authentication authentication,
                              @RequestParam String themeColor,
                              RedirectAttributes redirectAttributes) {
        studentService.updateTheme(currentStudent(authentication), themeColor);
        redirectAttributes.addFlashAttribute("success", "Тема применена");
        return "redirect:/profile";
    }

    @GetMapping("/excuses")
    public String excuses(Authentication authentication, Model model) {
        Student student = currentStudent(authentication);
        model.addAttribute("student", student);
        model.addAttribute("excuses", excuseService.forStudent(student));
        return "excuses";
    }

    @PostMapping("/excuses")
    public String createExcuse(Authentication authentication,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                               @RequestParam String reasonType,
                               @RequestParam(required = false) String comment,
                               @RequestParam(required = false) MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        try {
            excuseService.create(currentStudent(authentication), dateFrom, dateTo, reasonType, comment, file);
            redirectAttributes.addFlashAttribute("success", "Справка добавлена, староста её увидит");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/excuses";
    }

    private Student currentStudent(Authentication authentication) {
        return studentService.findByRecordBook(authentication.getName());
    }
}
