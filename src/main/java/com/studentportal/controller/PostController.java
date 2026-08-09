package com.studentportal.controller;

import com.studentportal.model.Student;
import com.studentportal.service.PostService;
import com.studentportal.service.StudentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostController {
    private final PostService postService;
    private final StudentService studentService;

    public PostController(PostService postService, StudentService studentService) {
        this.postService = postService;
        this.studentService = studentService;
    }

    @GetMapping("/posts")
    public String posts(Authentication authentication, Model model) {
        Student student = studentService.findByRecordBook(authentication.getName());
        model.addAttribute("student", student);
        model.addAttribute("posts", postService.findVisibleForSubgroup(student.getSubgroup()));
        return "posts";
    }
}
