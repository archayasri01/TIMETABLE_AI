
package com.example.demo.service;

import com.example.demo.entity.Classroom;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.ClassroomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    public ClassroomService(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    public List<Classroom> getAllClassrooms() {
        try {
            return classroomRepository.findAll();
        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public Classroom getClassroomById(String id) {
        try {
            return classroomRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Classroom not found with ID: " + id
                            )
                    );
        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public Classroom createClassroom(Classroom classroom) {

        validateCapacity(classroom.getCapacity());

        try {
            classroom.setId(null);

            return classroomRepository.save(classroom);

        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public Classroom updateClassroom(
            String id, Classroom updatedClassroom) {

        Classroom existing = getClassroomById(id);

        validateCapacity(updatedClassroom.getCapacity());

        existing.setBuilding(updatedClassroom.getBuilding());
        existing.setRoomNumber(updatedClassroom.getRoomNumber());
        existing.setCapacity(updatedClassroom.getCapacity());

        try {
            return classroomRepository.save(existing);

        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public void deleteClassroom(String id) {

        getClassroomById(id);

        try {
            classroomRepository.deleteById(id);

        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    public long count() {
        try {
            return classroomRepository.count();

        } catch (ExecutionException | InterruptedException e) {
            throw handleDatabaseError(e);
        }
    }

    private void validateCapacity(Integer capacity) {

        if (capacity == null || capacity <= 0) {
            throw new ValidationException(
                    "Classroom capacity must be positive"
            );
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
