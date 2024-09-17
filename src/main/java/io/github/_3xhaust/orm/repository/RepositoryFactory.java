package io.github._3xhaust.orm.repository;

public interface RepositoryFactory {
    <T> Repository<T> getRepository(Class<T> entityClass);
}