
package com.example.demo.repository;

import com.example.demo.entity.Course;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class CourseRepository {

    private final Firestore firestore;

    public CourseRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Course save(Course course)
            throws ExecutionException, InterruptedException {

        String id = course.getId();

        if (id == null || id.isBlank()) {
            id = firestore.collection("courses")
                    .document()
                    .getId();

            course.setId(id);
        }

        firestore.collection("courses")
                .document(id)
                .set(course)
                .get();

        return course;
    }

    public Optional<Course> findById(String id)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot document = firestore
                .collection("courses")
                .document(id)
                .get()
                .get();

        if (document.exists()) {
            Course course = document.toObject(Course.class);

            if (course != null) {
                course.setId(document.getId());
                return Optional.of(course);
            }
        }

        return Optional.empty();
    }

    public List<Course> findAll()
            throws ExecutionException, InterruptedException {

        List<Course> courses = new ArrayList<>();

        List<QueryDocumentSnapshot> documents = firestore
                .collection("courses")
                .get()
                .get()
                .getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            Course course = document.toObject(Course.class);

            if (course != null) {
                course.setId(document.getId());
                courses.add(course);
            }
        }

        return courses;
    }

    public Optional<Course> findByCourseCode(String courseCode)
            throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> documents = firestore
                .collection("courses")
                .whereEqualTo("courseCode", courseCode)
                .limit(1)
                .get()
                .get()
                .getDocuments();

        if (!documents.isEmpty()) {
            QueryDocumentSnapshot document = documents.get(0);

            Course course = document.toObject(Course.class);

            if (course != null) {
                course.setId(document.getId());
                return Optional.of(course);
            }
        }

        return Optional.empty();
    }

    public boolean existsByCourseCode(String courseCode)
            throws ExecutionException, InterruptedException {

        return findByCourseCode(courseCode).isPresent();
    }

    public boolean existsById(String id)
            throws ExecutionException, InterruptedException {

        return findById(id).isPresent();
    }

    public void deleteById(String id)
            throws ExecutionException, InterruptedException {

        firestore.collection("courses")
                .document(id)
                .delete()
                .get();
    }

    public long count()
            throws ExecutionException, InterruptedException {

        return firestore.collection("courses")
                .count()
                .get()
                .get()
                .getCount();
    }
}
