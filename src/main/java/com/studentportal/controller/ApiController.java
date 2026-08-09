package com.studentportal.controller;

import com.studentportal.dto.*;
import com.studentportal.model.Attendance;
import com.studentportal.model.Student;
import com.studentportal.service.ApiAuthService;
import com.studentportal.service.AttendanceService;
import com.studentportal.service.ScheduleService;
import com.studentportal.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final ApiAuthService apiAuthService;
    private final StudentService studentService;
    private final ScheduleService scheduleService;
    private final AttendanceService attendanceService;

    public ApiController(ApiAuthService apiAuthService,
                         StudentService studentService,
                         ScheduleService scheduleService,
                         AttendanceService attendanceService) {
        this.apiAuthService = apiAuthService;
        this.studentService = studentService;
        this.scheduleService = scheduleService;
        this.attendanceService = attendanceService;
    }

    @GetMapping("/student/{recordBook}")
    public StudentDto student(@RequestHeader(value = "X-BOT-API-KEY", required = false) String key,
                              @PathVariable String recordBook) {
        apiAuthService.requireBotKey(key);
        return StudentDto.from(studentService.findByRecordBook(recordBook));
    }

    @GetMapping("/schedule/today")
    public ScheduleDto todaySchedule(@RequestHeader(value = "X-BOT-API-KEY", required = false) String key,
                                      @RequestParam String recordBook) {
        apiAuthService.requireBotKey(key);
        Student student = studentService.findByRecordBook(recordBook);
        return scheduleService.findTodayForStudent(student).map(ScheduleDto::from).orElse(null);
    }

    @PostMapping("/attendance/mark")
    public AttendanceDto mark(@RequestHeader(value = "X-BOT-API-KEY", required = false) String key,
                              @RequestBody AttendanceMarkRequest request) {
        apiAuthService.requireBotKey(key);
        Student student = studentService.findByRecordBook(request.getRecordBookNumber());
        Attendance attendance = attendanceService.mark(student);
        return AttendanceDto.from(attendance);
    }

    @GetMapping("/attendance/history")
    public List<AttendanceDto> history(@RequestHeader(value = "X-BOT-API-KEY", required = false) String key,
                                        @RequestParam String recordBook) {
        apiAuthService.requireBotKey(key);
        Student student = studentService.findByRecordBook(recordBook);
        return attendanceService.history(student).stream().map(AttendanceDto::from).toList();
    }
}
