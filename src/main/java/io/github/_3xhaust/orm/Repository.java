package io.github._3xhaust.orm;

import java.util.List;
import java.util.Map;

public interface Repository<T> {
    T save(T entity);
    List<T> find();
    List<T> findOne(Map<String, Object> where);
    void delete(T entity);
    void update(T entity);
    T create(Object dto);
}
