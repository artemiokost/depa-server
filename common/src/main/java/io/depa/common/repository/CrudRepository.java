package io.depa.common.repository;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.util.List;

/**
 * Repository interface for CRUD operations.
 *
 * @author Artem Kostritsa
 */
public interface CrudRepository<T> {

    Completable deleteAll();

    Completable deleteById(Long id);

    Maybe<List<T>> findAll();

    Maybe<T> findById(Long id);

    Single<Long> save(T target);

    Completable updateById(Long id, T target);
}
