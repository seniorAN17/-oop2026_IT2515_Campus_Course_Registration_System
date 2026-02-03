package edu.aitu.oop3.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class InMemoryRepository<T> implements Repository<T> {
    private final List<T> storage = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void save(T item) {
        storage.add(item);
    }

    @Override
    public List<T> findAll() {
        synchronized (storage) {
            return new ArrayList<>(storage);
        }
    }

    @Override
    public List<T> find(Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        synchronized (storage) {
            for (T t : storage) if (predicate.test(t)) result.add(t);
        }
        return result;
    }

    @Override
    public Optional<T> findFirst(Predicate<T> predicate) {
        synchronized (storage) {
            for (T t : storage) if (predicate.test(t)) return Optional.of(t);
        }
        return Optional.empty();
    }

    @Override
    public void delete(T item) {
        storage.remove(item);
    }
}
