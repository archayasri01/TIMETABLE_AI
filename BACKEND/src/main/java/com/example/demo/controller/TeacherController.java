
package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Teacher;
import com.example.demo.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin(origins = "*")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable String id) {
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }

    @PostMapping
    public ResponseEntity<Teacher> createTeacher(
            @Valid @RequestBody Teacher teacher) {

        Teacher created = teacherService.createTeacher(teacher);

        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(
            @PathVariable String id,
            @Valid @RequestBody Teacher teacher) {

        return ResponseEntity.ok(
                teacherService.updateTeacher(id, teacher)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTeacher(
            @PathVariable String id) {

        teacherService.deleteTeacher(id);

        return ResponseEntity.ok(
                ApiResponse.success("Teacher deleted successfully")
        );
    }
}
