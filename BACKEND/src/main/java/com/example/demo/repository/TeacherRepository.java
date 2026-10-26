
package com.example.demo.repository;

import com.example.demo.entity.Teacher;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class TeacherRepository {

    private final Firestore firestore;

    public TeacherRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Teacher save(Teacher teacher)
            throws ExecutionException, InterruptedException {

        String id = teacher.getId();

        if (id == null || id.isBlank()) {
            id = firestore.collection("teachers")
                    .document()
                    .getId();

            teacher.setId(id);
        }

        firestore.collection("teachers")
                .document(id)
                .set(teacher)
                .get();

        return teacher;
    }

    public Optional<Teacher> findById(String id)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot document = firestore
                .collection("teachers")
                .document(id)
                .get()
                .get();

        if (document.exists()) {
            Teacher teacher = document.toObject(Teacher.class);

            if (teacher != null) {
                teacher.setId(document.getId());
                return Optional.of(teacher);
            }
        }

        return Optional.empty();
    }

    public List<Teacher> findAll()
            throws ExecutionException, InterruptedException {

        List<Teacher> teachers = new ArrayList<>();

        List<QueryDocumentSnapshot> documents = firestore
                .collection("teachers")
                .get()
                .get()
                .getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            Teacher teacher = document.toObject(Teacher.class);

            if (teacher != null) {
                teacher.setId(document.getId());
                teachers.add(teacher);
            }
        }

        return teachers;
    }

    public List<Teacher> findByDepartment(String department)
            throws ExecutionException, InterruptedException {

        List<Teacher> teachers = new ArrayList<>();

        List<QueryDocumentSnapshot> documents = firestore
                .collection("teachers")
                .whereEqualTo("department", department)
                .get()
                .get()
                .getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            Teacher teacher = document.toObject(Teacher.class);

            if (teacher != null) {
                teacher.setId(document.getId());
                teachers.add(teacher);
            }
        }

        return teachers;
    }

    public boolean existsById(String id)
            throws ExecutionException, InterruptedException {

        return findById(id).isPresent();
    }

    public void deleteById(String id)
            throws ExecutionException, InterruptedException {

        firestore.collection("teachers")
                .document(id)
                .delete()
                .get();
    }

    public long count()
            throws ExecutionException, InterruptedException {

        return firestore.collection("teachers")
                .count()
                .get()
                .get()
                .getCount();
    }
}
