package com.api.agent.controller;

import com.api.agent.dto.entity.Student;
import com.api.agent.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public String createStudent(@ModelAttribute Student student) {
        studentService.addStudent(student);
        return "{\"status\": \"success\", \"message\": \"学生添加成功\"}";
    }

    @PutMapping("/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute Student student) {
        student.setId(id);
        studentService.updateStudent(student);
        return "{\"status\": \"success\", \"message\": \"学生更新成功\"}";
    }

    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "{\"status\": \"success\", \"message\": \"学生删除成功\"}";
    }

    @GetMapping("/{id}")
    public Student getStudent(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }
}