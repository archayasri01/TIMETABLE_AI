package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.concurrent.ExecutionException;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final TimetableRepository timetableRepository;

    public DataInitializer(
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            CourseRepository courseRepository,
            ClassroomRepository classroomRepository,
            TimetableRepository timetableRepository) {

        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.timetableRepository = timetableRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (studentRepository.count() == 0) {
            seedStudents();
        }

        if (teacherRepository.count() == 0) {
            seedTeachers();
        }

        if (classroomRepository.count() == 0) {
            seedClassrooms();
        }

        if (courseRepository.count() == 0) {
            seedCourses();
        }

        if (timetableRepository.count() == 0) {
            seedTimetables();
        }
    }

    private void seedStudents()
            throws ExecutionException, InterruptedException {

        studentRepository.save(
                new Student("Archa", "student@gmail.com", "CSE", 3)
        );

        studentRepository.save(
                new Student("Rahul Sharma", "rahul.s@college.edu", "CSE", 3)
        );

        studentRepository.save(
                new Student("Sneha Patel", "sneha.p@college.edu", "ECE", 2)
        );

        studentRepository.save(
                new Student("Vikram Singh", "vikram.s@college.edu", "MECH", 4)
        );
    }

    private void seedTeachers()
            throws ExecutionException, InterruptedException {

        teacherRepository.save(
                new Teacher("Arun", "Java Programming", "CSE")
        );

        teacherRepository.save(
                new Teacher("Priya", "Database Management Systems", "CSE")
        );

        teacherRepository.save(
                new Teacher("Dr. Rajesh", "Computer Networks", "CSE")
        );

        teacherRepository.save(
                new Teacher("Prof. Anita", "Web Technologies", "IT")
        );
    }

    private void seedClassrooms()
            throws ExecutionException, InterruptedException {

        classroomRepository.save(
                new Classroom("Block A", "C101", 60)
        );

        classroomRepository.save(
                new Classroom("Block A", "C102", 50)
        );

        classroomRepository.save(
                new Classroom("Block B", "C103", 40)
        );

        classroomRepository.save(
                new Classroom("Tech Block", "Lab 201", 35)
        );
    }

    private void seedCourses()
            throws ExecutionException, InterruptedException {

        courseRepository.save(
                new Course("CS301", "Java Programming", "CSE", 4)
        );

        courseRepository.save(
                new Course("CS302", "Database Management Systems", "CSE", 4)
        );

        courseRepository.save(
                new Course("CS303", "Computer Networks", "CSE", 3)
        );

        courseRepository.save(
                new Course("IT201", "Web Technologies", "IT", 3)
        );
    }

    private void seedTimetables()
            throws ExecutionException, InterruptedException {

        Teacher arun = teacherRepository.findAll().stream()
                .filter(t -> t.getName().equalsIgnoreCase("Arun"))
                .findFirst()
                .orElse(null);

        Teacher priya = teacherRepository.findAll().stream()
                .filter(t -> t.getName().equalsIgnoreCase("Priya"))
                .findFirst()
                .orElse(null);

        Course java = courseRepository
                .findByCourseCode("CS301")
                .orElse(null);

        Course dbms = courseRepository
                .findByCourseCode("CS302")
                .orElse(null);

        Classroom c101 = classroomRepository.findAll().stream()
                .filter(c -> c.getRoomNumber().equalsIgnoreCase("C101"))
                .findFirst()
                .orElse(null);

        Classroom c102 = classroomRepository.findAll().stream()
                .filter(c -> c.getRoomNumber().equalsIgnoreCase("C102"))
                .findFirst()
                .orElse(null);

        if (arun != null && priya != null &&
                java != null && dbms != null &&
                c101 != null && c102 != null) {

            timetableRepository.save(
                    new Timetable("Monday", LocalTime.of(9, 0),
                            LocalTime.of(10, 0), arun, java, c101, false)
            );

            timetableRepository.save(
                    new Timetable("Monday", LocalTime.of(11, 0),
                            LocalTime.of(12, 0), priya, dbms, c102, false)
            );

            timetableRepository.save(
                    new Timetable("Tuesday", LocalTime.of(9, 0),
                            LocalTime.of(10, 0), priya, dbms, c102, false)
            );

            timetableRepository.save(
                    new Timetable("Tuesday", LocalTime.of(10, 0),
                            LocalTime.of(11, 0), arun, java, c101, false)
            );

            timetableRepository.save(
                    new Timetable("Wednesday", LocalTime.of(10, 0),
                            LocalTime.of(11, 0), priya, dbms, c102, false)
            );

            timetableRepository.save(
                    new Timetable("Wednesday", LocalTime.of(11, 0),
                            LocalTime.of(12, 0), arun, java, c101, false)
            );

            timetableRepository.save(
                    new Timetable("Thursday", LocalTime.of(9, 0),
                            LocalTime.of(10, 0), arun, java, c101, false)
            );

            timetableRepository.save(
                    new Timetable("Thursday", LocalTime.of(11, 0),
                            LocalTime.of(12, 0), priya, dbms, c102, false)
            );

            timetableRepository.save(
                    new Timetable("Friday", LocalTime.of(10, 0),
                            LocalTime.of(11, 0), arun, java, c101, false)
            );
        }
    }
}