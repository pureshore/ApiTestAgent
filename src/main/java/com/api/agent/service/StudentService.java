package com.api.agent.service;

import com.api.agent.dto.entity.Student;
import java.util.List;

public interface StudentService {
    void addStudent(Student student);
    void updateStudent(Student student);
    void deleteStudent(Long id);
    Student getStudentById(Long id);
    List<Student> getAllStudents();
}