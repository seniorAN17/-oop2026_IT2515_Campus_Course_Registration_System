package edu.aitu.oop3.db;

@FunctionalInterface
public interface RegistrationCallback {
    void onRegistered(DemoUsersExample.Student student, Course course);
}
