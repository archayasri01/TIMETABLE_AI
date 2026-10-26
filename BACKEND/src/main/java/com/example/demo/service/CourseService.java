package com.example.demo.service;

import com.example.demo.entity.Course;
import com.example.demo.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> getAllCourses() {
        try {
            return courseRepository.findAll();
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to fetch courses", e);
        }
    }

    public Course getCourseById(String id) {
        try {
            return courseRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Course not found: " + id));
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to fetch course", e);
        }
    }

    public Course createCourse(Course course) {
        try {
            return courseRepository.save(course);
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to create course", e);
        }
    }

    public Course updateCourse(String id, Course updatedCourse) {
        try {
            Course existingCourse = courseRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Course not found: " + id));

            updatedCourse.setId(existingCourse.getId());

            return courseRepository.save(updatedCourse);
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to update course", e);
        }
    }

    public void deleteCourse(String id) {
        try {
            if (!courseRepository.existsById(id)) {
                throw new RuntimeException("Course not found: " + id);
            }

            courseRepository.deleteById(id);
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to delete course", e);
        }
    }
}