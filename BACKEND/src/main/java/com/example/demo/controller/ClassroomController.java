
package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Classroom;
import com.example.demo.service.ClassroomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@CrossOrigin(origins = "*")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping
    public ResponseEntity<List<Classroom>> getAllClassrooms() {
        return ResponseEntity.ok(
                classroomService.getAllClassrooms()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Classroom> getClassroomById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                classroomService.getClassroomById(id)
        );
    }

    @PostMapping
    public ResponseEntity<Classroom> createClassroom(
            @Valid @RequestBody Classroom classroom) {

        Classroom created = classroomService.createClassroom(classroom);

        return new ResponseEntity<>(
                created,
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Classroom> updateClassroom(
            @PathVariable String id,
            @Valid @RequestBody Classroom classroom) {

        return ResponseEntity.ok(
                classroomService.updateClassroom(id, classroom)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteClassroom(
            @PathVariable String id) {

        classroomService.deleteClassroom(id);

        return ResponseEntity.ok(
                ApiResponse.success("Classroom deleted successfully")
        );
    }
}
