
package com.example.demo.service;

import com.example.demo.entity.Classroom;
import com.example.demo.entity.Course;
import com.example.demo.entity.Teacher;
import com.example.demo.entity.Timetable;
import com.example.demo.repository.ClassroomRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.TeacherRepository;
import com.example.demo.repository.TimetableRepository;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class TimetableGeneratorService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final TimetableRepository timetableRepository;

    private static final String[] DAYS = {
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday"
    };

    private static final LocalTime[][] SLOTS = {
            {LocalTime.of(9, 0), LocalTime.of(10, 0)},
            {LocalTime.of(10, 0), LocalTime.of(11, 0)},
            {LocalTime.of(11, 0), LocalTime.of(12, 0)},
            {LocalTime.of(12, 0), LocalTime.of(13, 0)},
            {LocalTime.of(14, 0), LocalTime.of(15, 0)},
            {LocalTime.of(15, 0), LocalTime.of(16, 0)},
            {LocalTime.of(16, 0), LocalTime.of(17, 0)}
    };

    public TimetableGeneratorService(
            CourseRepository courseRepository,
            TeacherRepository teacherRepository,
            ClassroomRepository classroomRepository,
            TimetableRepository timetableRepository) {

        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
        this.timetableRepository = timetableRepository;
    }

    public List<Timetable> generateTimetable() {

        try {
            return generateFromFirestore();

        } catch (ExecutionException | InterruptedException e) {

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw new IllegalStateException(
                    "Firebase timetable generation failed", e
            );
        }
    }

    private List<Timetable> generateFromFirestore()
            throws ExecutionException, InterruptedException {

        List<Timetable> generated = new ArrayList<>();

        List<Course> courses = courseRepository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();

        List<Timetable> existing = timetableRepository.findAll();

        if (courses.isEmpty() ||
                teachers.isEmpty() ||
                classrooms.isEmpty()) {

            return generated;
        }

        // Keep existing timetable records unchanged.
        // Count existing weekly periods for each course.
        Map<String, Integer> periodCounts = new HashMap<>();

        // Track days on which each course is already scheduled.
        Map<String, Set<String>> courseDays = new HashMap<>();

        for (Timetable timetable : existing) {

            if (timetable.getCourse() == null ||
                    timetable.getCourse().getId() == null) {
                continue;
            }

            String courseId = timetable.getCourse().getId();

            periodCounts.merge(courseId, 1, Integer::sum);

            if (timetable.getDay() != null) {
                courseDays
                        .computeIfAbsent(
                                courseId,
                                key -> new HashSet<>()
                        )
                        .add(timetable.getDay());
            }
        }

        // Use a predictable ordering.
        courses.sort(
                Comparator.comparing(
                        Course::getCourseCode,
                        Comparator.nullsLast(String::compareTo)
                )
        );

        classrooms.sort(
                Comparator.comparing(
                        Classroom::getRoomNumber,
                        Comparator.nullsLast(String::compareTo)
                )
        );

        for (Course course : courses) {

            if (course.getId() == null) {
                continue;
            }

            String courseId = course.getId();

            // Assumption: one credit corresponds to
            // one teaching period per week.
            int requiredPeriods = Math.max(0, course.getCredits());

            int existingPeriods =
                    periodCounts.getOrDefault(courseId, 0);

            int remainingPeriods =
                    Math.max(0, requiredPeriods - existingPeriods);

            if (remainingPeriods == 0) {
                continue;
            }

            // Find teachers whose subject matches the course.
            List<Teacher> matchingTeachers = new ArrayList<>();

            for (Teacher teacher : teachers) {

                if (teacher.getSubject() == null ||
                        course.getCourseName() == null) {
                    continue;
                }

                boolean subjectMatches =
                        teacher.getSubject().trim()
                                .equalsIgnoreCase(
                                        course.getCourseName().trim()
                                );

                boolean departmentMatches =
                        teacher.getDepartment() != null &&
                        course.getDepartment() != null &&
                        teacher.getDepartment().trim()
                                .equalsIgnoreCase(
                                        course.getDepartment().trim()
                                );

                if (subjectMatches && departmentMatches) {
                    matchingTeachers.add(teacher);
                }
            }

            // Do not assign an unrelated teacher.
            if (matchingTeachers.isEmpty()) {

                System.out.println(
                        "No matching teacher for course: " +
                        course.getCourseName()
                );

                continue;
            }

            Teacher teacher = matchingTeachers.get(0);

            Set<String> scheduledDays =
                    courseDays.computeIfAbsent(
                            courseId,
                            key -> new HashSet<>()
                    );

            // First prefer days without this course.
            // Then allow additional periods on the same day.
            for (int pass = 0;
                    pass < 2 && remainingPeriods > 0;
                    pass++) {

                for (String day : DAYS) {

                    if (remainingPeriods == 0) {
                        break;
                    }

                    boolean alreadyOnDay =
                            scheduledDays.contains(day);

                    if (pass == 0 && alreadyOnDay) {
                        continue;
                    }

                    if (pass == 1 && !alreadyOnDay) {
                        continue;
                    }

                    boolean scheduledThisDay = false;

                    for (LocalTime[] slot : SLOTS) {

                        if (scheduledThisDay) {
                            break;
                        }

                        LocalTime startTime = slot[0];
                        LocalTime endTime = slot[1];

                        long teacherConflicts =
                                timetableRepository.countTeacherConflict(
                                        teacher.getId(),
                                        day,
                                        startTime,
                                        endTime,
                                        null
                                );

                        if (teacherConflicts > 0) {
                            continue;
                        }

                        long courseConflicts =
                                timetableRepository.countCourseConflict(
                                        courseId,
                                        day,
                                        startTime,
                                        endTime,
                                        null
                                );

                        if (courseConflicts > 0) {
                            continue;
                        }

                        for (Classroom classroom : classrooms) {

                            long classroomConflicts =
                                    timetableRepository.countClassroomConflict(
                                            classroom.getId(),
                                            day,
                                            startTime,
                                            endTime,
                                            null
                                    );

                            if (classroomConflicts > 0) {
                                continue;
                            }

                            Timetable timetable = new Timetable();

                            timetable.setDay(day);
                            timetable.setStartTime(startTime);
                            timetable.setEndTime(endTime);

                            timetable.setTeacher(teacher);
                            timetable.setCourse(course);
                            timetable.setClassroom(classroom);

                            timetable.setAutoGenerated(true);

                            Timetable saved =
                                    timetableRepository.save(timetable);

                            generated.add(saved);

                            scheduledDays.add(day);

                            remainingPeriods--;
                            scheduledThisDay = true;

                            break;
                        }
                    }
                }
            }

            if (remainingPeriods > 0) {
                System.out.println(
                        "Could not schedule " +
                        remainingPeriods +
                        " period(s) for " +
                        course.getCourseName()
                );
            }
        }

        return generated;
    }
}
