
package com.example.demo.repository;

import com.example.demo.entity.Classroom;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class ClassroomRepository {

    private final Firestore firestore;

    public ClassroomRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Classroom save(Classroom classroom)
            throws ExecutionException, InterruptedException {

        String id = classroom.getId();

        if (id == null || id.isBlank()) {
            id = firestore.collection("classrooms")
                    .document()
                    .getId();

            classroom.setId(id);
        }

        firestore.collection("classrooms")
                .document(id)
                .set(classroom)
                .get();

        return classroom;
    }

    public Optional<Classroom> findById(String id)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot document = firestore
                .collection("classrooms")
                .document(id)
                .get()
                .get();

        if (document.exists()) {
            Classroom classroom = document.toObject(Classroom.class);

            if (classroom != null) {
                classroom.setId(document.getId());
                return Optional.of(classroom);
            }
        }

        return Optional.empty();
    }

    public List<Classroom> findAll()
            throws ExecutionException, InterruptedException {

        List<Classroom> classrooms = new ArrayList<>();

        List<QueryDocumentSnapshot> documents = firestore
                .collection("classrooms")
                .get()
                .get()
                .getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            Classroom classroom = document.toObject(Classroom.class);

            if (classroom != null) {
                classroom.setId(document.getId());
                classrooms.add(classroom);
            }
        }

        return classrooms;
    }

    public Optional<Classroom> findByBuildingAndRoomNumber(
            String building, String roomNumber)
            throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> documents = firestore
                .collection("classrooms")
                .whereEqualTo("building", building)
                .whereEqualTo("roomNumber", roomNumber)
                .limit(1)
                .get()
                .get()
                .getDocuments();

        if (!documents.isEmpty()) {
            QueryDocumentSnapshot document = documents.get(0);

            Classroom classroom = document.toObject(Classroom.class);

            if (classroom != null) {
                classroom.setId(document.getId());
                return Optional.of(classroom);
            }
        }

        return Optional.empty();
    }

    public boolean existsById(String id)
            throws ExecutionException, InterruptedException {

        return findById(id).isPresent();
    }

    public void deleteById(String id)
            throws ExecutionException, InterruptedException {

        firestore.collection("classrooms")
                .document(id)
                .delete()
                .get();
    }

    public long count()
            throws ExecutionException, InterruptedException {

        return firestore.collection("classrooms")
                .count()
                .get()
                .get()
                .getCount();
    }
}
