package edu.aitu.oop3.db;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Repository<T> {
    void save(T item);
    List<T> findAll();
    List<T> find(Predicate<T> predicate);
    Optional<T> findFirst(Predicate<T> predicate);
    void delete(T item);
}
