package io.depa.user.reposirory;

import io.depa.common.repository.impl.AsyncRepository;
import io.depa.user.model.Role;
import io.depa.user.query.RoleQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class RoleRepository extends AsyncRepository<Role> {

    public RoleRepository() {
        super(Role.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(RoleQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long roleId) {
        Tuple arguments = Tuple.of(roleId);
        return delete(RoleQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Role>> findAll() {
        return findAll(RoleQuery.SELECT_BY_ID);
    }

    @Override
    public Maybe<Role> findById(Long roleId) {
        Tuple arguments = Tuple.of(roleId);
        return find(RoleQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Role role) {
        return save(RoleQuery.INSERT, Tuple.of(role.getName()));
    }

    @Override
    public Completable updateById(Long roleId, Role newRole) {
        return findById(roleId)
                .map(old -> old.merge(newRole))
                .map(merged -> Tuple.of(merged.getName(), merged.getId()))
                .flatMapCompletable(arguments -> update(RoleQuery.UPDATE_BY_ID, arguments));
    }

    public Maybe<Role> findByName(String roleName) {
        Tuple arguments = Tuple.of(roleName);
        return find(RoleQuery.SELECT_BY_NAME, arguments);
    }

    public Maybe<List<String>> findByUserId(Long userId) {
        Tuple arguments = Tuple.of(userId);
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(RoleQuery.SELECT_BY_USER).rxExecute(arguments)
                        .filter(rows -> rows.size() != 0)
                        .toObservable()
                        .flatMapIterable(e -> e)
                        .map(row -> row.getString("name"))
                        .toList()
                        .toMaybe()
                        .doFinally(connection::close));
    }

    public Maybe<List<String>> findByUsername(String username) {
        Tuple arguments = Tuple.of(username);
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(RoleQuery.SELECT_BY_USERNAME).rxExecute(arguments)
                        .filter(rows -> rows.size() != 0)
                        .toObservable()
                        .flatMapIterable(e -> e)
                        .map(row -> row.getString("name"))
                        .toList()
                        .toMaybe()
                        .doFinally(connection::close));
    }

    public Single<Long> saveByUser(Long userId, Role role) {
        Tuple arguments = Tuple.of(role.getId(), userId);
        return save(RoleQuery.INSERT_ROLE_USER, arguments);
    }
}
