package io.depa.common.repository.impl;

import io.depa.common.context.ApplicationContext;
import io.depa.common.data.Page;
import io.depa.common.repository.CrudRepository;
import io.depa.common.util.Constants;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.Pool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.Tuple;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for async operations.
 *
 * @author Artem Kostritsa
 */
public abstract class AsyncRepository<T> implements CrudRepository<T> {

    protected Pool pool;
    protected Constructor<T> typeClassConstructor;

    protected AsyncRepository(Class<T> typeClass) {
        this.pool = ApplicationContext.getMySQLPool();
        try {
            this.typeClassConstructor = typeClass.getConstructor(JsonObject.class);
        } catch (NoSuchMethodException ignored) {}
    }

    protected static class Arguments {

        Tuple tuple = Tuple.tuple();

        public static Arguments builder() {
            return new Arguments();
        }

        private static void setObjectOrNull(Object object, Tuple tuple) {
            if (object == null) {
                tuple.addValue(null);
                return;
            }
            if (!(object instanceof String)) {
                tuple.addValue(object);
                return;
            }
            if (object.equals(Constants.EMPTY)) {
                tuple.addValue(null);
            } else {
                tuple.addValue(object);
            }
        }

        public Arguments add(Object object) {
            setObjectOrNull(object, tuple);
            return this;
        }

        public Tuple build() {
            return tuple;
        }
    }

    protected Integer calcPage(Integer number, Integer size) {
        if (number <= 1) return 0;
        return (number - 1) * size;
    }

    protected JsonObject mapRowToJson(Row row) {
        int size = row.size();
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < size; i++) {
            String columnName = row.getColumnName(i);
            Object value = row.getValue(i);
            if (value != null) {
                if (value instanceof Byte) {
                    value = (Byte) value == 1;
                }
                if (value instanceof LocalDateTime) {
                    value = ((LocalDateTime) value).format(Constants.DATE_TIME_FORMATTER);
                }
                jsonObject.put(columnName, value);
            }
        }
        return jsonObject;
    }

    protected Completable delete(String query, Tuple arguments) {
        return this.pool.rxGetConnection().flatMapCompletable(connection ->
                connection.preparedQuery(query).rxExecute(arguments)
                        .ignoreElement()
                        .doFinally(connection::close));
    }

    protected Completable deleteAll(String query) {
        return this.pool.rxGetConnection().flatMapCompletable(connection ->
                connection.preparedQuery(query).rxExecute()
                        .ignoreElement()
                        .doFinally(connection::close));
    }

    protected Maybe<T> find(String query, Tuple arguments) {
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(query).rxExecute(arguments)
                        .filter(rows -> rows.size() != 0)
                        .map(rows -> mapRowToJson(rows.iterator().next()))
                        .map(jsonObject -> this.typeClassConstructor.newInstance(jsonObject))
                        .doFinally(connection::close));
    }

    protected Maybe<List<T>> findAll(String query) {
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(query).rxExecute()
                        .filter(rows -> rows.size() != 0)
                        .toObservable()
                        .flatMapIterable(e -> e)
                        .map(this::mapRowToJson)
                        .map(this.typeClassConstructor::newInstance)
                        .toList()
                        .toMaybe()
                        .doFinally(connection::close));
    }

    protected Maybe<Page<T>> findPage(String query, Tuple arguments) {
        return this.pool.rxGetConnection().flatMapMaybe(connection -> {

            Maybe<List<T>> listMaybe = connection.preparedQuery(query).rxExecute(arguments)
                    .filter(rows -> rows.size() != 0)
                    .toObservable()
                    .flatMapIterable(e -> e)
                    .map(this::mapRowToJson)
                    .map(this.typeClassConstructor::newInstance)
                    .toList()
                    .toMaybe();

            Maybe<Integer> totalElementsMaybe = connection.preparedQuery("select found_rows()").rxExecute()
                    .map(rows -> rows.iterator().next().getInteger(0))
                    .toMaybe();

            Maybe<Page<T>> pageMaybe = listMaybe.zipWith(totalElementsMaybe, (list, totalElements) -> {
                Integer number = arguments.getInteger(arguments.size() - 2);
                Integer size = arguments.getInteger(arguments.size() - 1);
                number = (number + size) / size;
                return new Page<>(number, size, totalElements, list);
            });

            return connection.rxBegin().flatMapMaybe(transaction -> pageMaybe
                    .doOnSuccess(ignored -> transaction.commit())
                    .doOnError(ignored -> transaction.rollback())
                    .doFinally(connection::close));
        });
    }

    protected Single<Long> save(String query, Tuple arguments) {
        return this.pool.rxGetConnection().flatMap(connection -> {

            Single<Long> single = connection.preparedQuery(query).rxExecute(arguments)
                    .flatMap(ignored -> connection.preparedQuery("select last_insert_id()").rxExecute())
                    .map(rows -> rows.iterator().next().getLong(0));

            return connection.rxBegin().flatMap(transaction -> single
                    .doOnSuccess(ignored -> transaction.commit())
                    .doOnError(ignored -> transaction.rollback())
                    .doFinally(connection::close));
        });
    }

    protected Completable update(String query, Tuple arguments) {
        return this.pool.rxGetConnection().flatMapCompletable(connection ->
                connection.preparedQuery(query).rxExecute(arguments)
                        .ignoreElement()
                        .doFinally(connection::close));
    }
}
