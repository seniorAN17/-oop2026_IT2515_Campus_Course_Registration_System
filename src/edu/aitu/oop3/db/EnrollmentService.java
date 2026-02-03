package edu.aitu.oop3.db;

import java.util.List;
import java.util.function.Predicate;

public class EnrollmentService {
    private final Repository<DemoUsersExample.Student> studentRepository;
    private final RegistrationService registrationService;

    public EnrollmentService(Repository<DemoUsersExample.Student> studentRepository, RegistrationService registrationService) {
        this.studentRepository = studentRepository;
        this.registrationService = registrationService;
    }

    public void registerAll(Predicate<DemoUsersExample.Student> predicate, Course course, RegistrationCallback callback) {
        List<DemoUsersExample.Student> filtered = studentRepository.find(predicate);
        filtered.forEach(s -> {
            registrationService.register(s, course);
            if (callback != null) callback.onRegistered(s, course);
        });
    }
}
