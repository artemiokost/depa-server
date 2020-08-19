package io.depa.user.reposirory;

import io.depa.common.repository.impl.AsyncRepository;
import io.depa.user.model.User;
import io.depa.user.query.UserQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class UserRepository extends AsyncRepository<User> {

    public UserRepository() {
        super(User.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(UserQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long userId) {
        Tuple arguments = Tuple.of(userId);
        return delete(UserQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<User>> findAll() {
        return findAll(UserQuery.SELECT_ALL);
    }

    @Override
    public Maybe<User> findById(Long userId) {
        Tuple arguments = Tuple.of(userId);
        return find(UserQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(User user) {
        Tuple arguments = Arguments.builder()
                .add(user.getEmail())
                .add(user.getUsername())
                .add(user.getPassword())
                .add(user.getPasswordSalt())
                .add(user.getCreatedAt())
                .add(user.getUpdatedAt())
                .build();
        return save(UserQuery.INSERT, arguments);
    }

    @Override
    public Completable updateById(Long userId, User newUser) {
        return findById(userId)
                .map(old -> old.merge(newUser))
                .map(merged -> Arguments.builder()
                        .add(merged.getEmail())
                        .add(merged.getUsername())
                        .add(merged.getPassword())
                        .add(merged.getPasswordSalt())
                        .add(merged.getUpdatedAt())
                        .add(merged.getId())
                        .build())
                .flatMapCompletable(arguments -> update(UserQuery.UPDATE_BY_ID, arguments));
    }

    public Maybe<User> findByEmail(String email) {
        Tuple arguments = Tuple.of(email);
        return find(UserQuery.SELECT_BY_EMAIL, arguments);
    }

    public Maybe<User> findByUsername(String username) {
        Tuple arguments = Tuple.of(username);
        return find(UserQuery.SELECT_BY_USERNAME, arguments);
    }

    public Completable updateBannedById(Long userId, Boolean value) {
        Tuple arguments = Tuple.of(value, userId);
        return update(UserQuery.UPDATE_BANNED_BY_ID, arguments);
    }
}
