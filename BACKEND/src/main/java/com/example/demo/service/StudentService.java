
package com.example.demo.service;

import com.example.demo.entity.Student;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents() {
        try {
            return studentRepository.findAll();
        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public Student getStudentById(String id) {
        try {
            return studentRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Student not found with ID: " + id
                            )
                    );
        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public Student createStudent(Student student) {
        try {
            if (student.getEmail() != null &&
                    studentRepository.existsByEmail(student.getEmail())) {

                throw new ValidationException(
                        "Email is already registered: " + student.getEmail()
                );
            }

            student.setId(null);

            return studentRepository.save(student);

        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public Student updateStudent(String id, Student updatedStudent) {
        try {
            Student existing = getStudentById(id);

            if (updatedStudent.getEmail() != null &&
                    !updatedStudent.getEmail().equalsIgnoreCase(existing.getEmail())) {

                if (studentRepository.existsByEmail(updatedStudent.getEmail())) {
                    throw new ValidationException(
                            "Email is already registered: " + updatedStudent.getEmail()
                    );
                }

                existing.setEmail(updatedStudent.getEmail());
            }

            existing.setName(updatedStudent.getName());
            existing.setDepartment(updatedStudent.getDepartment());
            existing.setYear(updatedStudent.getYear());

            return studentRepository.save(existing);

        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public void deleteStudent(String id) {
        try {
            getStudentById(id);
            studentRepository.deleteById(id);

        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public long count() {
        return getAllStudents().size();
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
