
package com.example.demo.service;

import com.example.demo.entity.Teacher;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public List<Teacher> getAllTeachers() {
        try {
            return teacherRepository.findAll();
        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public Teacher getTeacherById(String id) {
        try {
            return teacherRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Teacher not found with ID: " + id
                            )
                    );
        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public Teacher createTeacher(Teacher teacher) {
        try {
            teacher.setId(null);
            return teacherRepository.save(teacher);
        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public Teacher updateTeacher(String id, Teacher updatedTeacher) {
        try {
            Teacher existing = getTeacherById(id);

            existing.setName(updatedTeacher.getName());
            existing.setSubject(updatedTeacher.getSubject());
            existing.setDepartment(updatedTeacher.getDepartment());

            return teacherRepository.save(existing);

        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public void deleteTeacher(String id) {
        try {
            getTeacherById(id);
            teacherRepository.deleteById(id);

        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public long count() {
        try {
            return teacherRepository.count();
        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    private RuntimeException handleDatabaseError(Exception e) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }

        return new IllegalStateException(
                "Firebase Firestore operation failed", e
        );
    }
}
