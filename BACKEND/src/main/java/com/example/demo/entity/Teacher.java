
package com.example.demo.entity;

import jakarta.validation.constraints.NotBlank;

public class Teacher {

    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Department is required")
    private String department;

    public Teacher() {
    }

    public Teacher(String id, String name, String subject, String department) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.department = department;
    }

    public Teacher(String name, String subject, String department) {
        this.name = name;
        this.subject = subject;
        this.department = department;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
