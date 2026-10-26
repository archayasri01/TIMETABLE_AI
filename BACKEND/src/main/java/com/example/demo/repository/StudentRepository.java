package com.example.demo.repository;

import com.example.demo.entity.Student;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class StudentRepository {

    private final Firestore firestore;

    public StudentRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Student save(Student student)
            throws ExecutionException, InterruptedException {

        String id = student.getId();

        if (id == null || id.isBlank()) {
            id = firestore.collection("students")
                    .document()
                    .getId();

            student.setId(id);
        }

        firestore.collection("students")
                .document(id)
                .set(student)
                .get();

        return student;
    }

    public Optional<Student> findById(String id)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot document = firestore
                .collection("students")
                .document(id)
                .get()
                .get();

        if (document.exists()) {
            Student student = document.toObject(Student.class);

            if (student != null) {
                student.setId(document.getId());
                return Optional.of(student);
            }
        }

        return Optional.empty();
    }

    public List<Student> findAll()
            throws ExecutionException, InterruptedException {

        List<Student> students = new ArrayList<>();

        List<QueryDocumentSnapshot> documents = firestore
                .collection("students")
                .get()
                .get()
                .getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            Student student = document.toObject(Student.class);
            student.setId(document.getId());
            students.add(student);
        }

        return students;
    }

    public Optional<Student> findByEmail(String email)
            throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> documents = firestore
                .collection("students")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .get()
                .getDocuments();

        if (!documents.isEmpty()) {
            QueryDocumentSnapshot document = documents.get(0);

            Student student = document.toObject(Student.class);
            student.setId(document.getId());

            return Optional.of(student);
        }

        return Optional.empty();
    }

    public boolean existsByEmail(String email)
            throws ExecutionException, InterruptedException {

        return findByEmail(email).isPresent();
    }

    public void deleteById(String id)
            throws ExecutionException, InterruptedException {

        firestore.collection("students")
                .document(id)
                .delete()
                .get();
    }

    public boolean existsById(String id)
            throws ExecutionException, InterruptedException {

        return findById(id).isPresent();
    }

    public long count() {
        try {
            return firestore.collection("students")
                    .get()
                    .get()
                    .size();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Student count interrupted", e
            );

        } catch (ExecutionException e) {
            throw new IllegalStateException(
                    "Failed to count students", e
            );
        }
    }
}