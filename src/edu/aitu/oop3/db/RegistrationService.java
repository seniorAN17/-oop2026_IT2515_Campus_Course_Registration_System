package edu.aitu.oop3.db;

public class RegistrationService {
    public void register(DemoUsersExample.Student student, Course course) {
        System.out.println("Student " + student + " registered for " + course.type + " course.");
    }
}