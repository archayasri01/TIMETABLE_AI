package com.example.demo;

import com.example.demo.dto.TimetableRequest;
import com.example.demo.exception.TimetableConflictException;
import com.example.demo.exception.ValidationException;
import com.example.demo.service.TimetableService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TimetableConflictTest {

    @Autowired
    private TimetableService timetableService;

    @Test
    @DisplayName("Should detect Teacher Conflict on overlapping times")
    void testTeacherConflict() {

        TimetableRequest request = new TimetableRequest(
                "Monday",
                LocalTime.of(9, 30),
                LocalTime.of(10, 30),
                false
        );

        TimetableConflictException ex = assertThrows(
                TimetableConflictException.class,
                () -> timetableService.createTimetable(
                        request, "1", "2", "2"
                )
        );

        assertTrue(ex.getMessage().contains("Teacher is already assigned"));
    }

    @Test
    @DisplayName("Should detect Classroom Conflict on overlapping times")
    void testClassroomConflict() {

        TimetableRequest request = new TimetableRequest(
                "Monday",
                LocalTime.of(9, 30),
                LocalTime.of(10, 30),
                false
        );

        TimetableConflictException ex = assertThrows(
                TimetableConflictException.class,
                () -> timetableService.createTimetable(
                        request, "2", "2", "1"
                )
        );

        assertTrue(ex.getMessage().contains("Classroom is already occupied"));
    }

    @Test
    @DisplayName("Should detect Course Conflict on overlapping times")
    void testCourseConflict() {

        TimetableRequest request = new TimetableRequest(
                "Monday",
                LocalTime.of(9, 30),
                LocalTime.of(10, 30),
                false
        );

        TimetableConflictException ex = assertThrows(
                TimetableConflictException.class,
                () -> timetableService.createTimetable(
                        request, "2", "1", "2"
                )
        );

        assertTrue(ex.getMessage().contains("Course already has a timetable"));
    }

    @Test
    @DisplayName("Should reject invalid timetable when start time is after end time")
    void testInvalidTime() {

        TimetableRequest request = new TimetableRequest(
                "Monday",
                LocalTime.of(14, 0),
                LocalTime.of(13, 0),
                false
        );

        assertThrows(
                ValidationException.class,
                () -> timetableService.createTimetable(
                        request, "1", "1", "1"
                )
        );
    }
}