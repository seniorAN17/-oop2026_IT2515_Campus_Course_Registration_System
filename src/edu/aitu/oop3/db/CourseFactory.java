package edu.aitu.oop3.db;

public class CourseFactory {
    public static Course createCourse(String type) {
        return new Course(type);
    }
}