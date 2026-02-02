package edu.aitu.oop3.db;

public class StudentBuilder {
    private String firstName;
    private String lastName;
    private String email;
    private String studentNumber;

    public StudentBuilder setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
    public StudentBuilder setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public StudentBuilder setEmail(String email) {
        this.email = email;
        return this;
    }
//h
    public StudentBuilder setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
        return this;
    }

    public DemoUsersExample.Student build() {
        return new DemoUsersExample.Student(firstName, lastName, email, studentNumber);
    }
}

