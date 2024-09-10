package io.github._3xhaust.orm;

public interface RepositoryFactory {
    <T> Repository<T> getRepository(Class<T> entityClass);
}