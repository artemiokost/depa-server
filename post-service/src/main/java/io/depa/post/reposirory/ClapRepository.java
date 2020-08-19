package io.depa.post.reposirory;

import io.depa.common.repository.impl.AsyncRepository;
import io.depa.post.model.Clap;
import io.depa.post.query.ClapQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class ClapRepository extends AsyncRepository<Clap> {

    public ClapRepository() {
        super(Clap.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(ClapQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long clapId) {
        Tuple arguments = Tuple.of(clapId);
        return delete(ClapQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Clap>> findAll() {
        return findAll(ClapQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Clap> findById(Long clapId) {
        Tuple arguments = Tuple.of(clapId);
        return find(ClapQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Clap clap) {
        Tuple arguments = Tuple.of(
                clap.getPostId(),
                clap.getValue(),
                clap.getCreatedAt(),
                clap.getUpdatedAt(),
                clap.getCreatedBy(),
                clap.getUpdatedBy());
        return save(ClapQuery.INSERT, arguments);
    }

    @Override
    public Completable updateById(Long clapId, Clap newClap) {
        return findById(clapId)
                .map(old -> old.merge(newClap))
                .map(merged -> Tuple.of(
                        merged.getValue(),
                        merged.getUpdatedAt(),
                        merged.getUpdatedBy(),
                        merged.getId()))
                .flatMapCompletable(arguments -> update(ClapQuery.UPDATE_BY_ID, arguments));
    }

    public Maybe<List<Clap>> findByPostId(Long postId) {
        Tuple arguments = Tuple.of(postId);
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(ClapQuery.SELECT_BY_POST).rxExecute(arguments)
                        .filter(rows -> rows.size() != 0)
                        .toObservable()
                        .flatMapIterable(e -> e)
                        .map(this::mapRowToJson)
                        .map(Clap::new)
                        .toList()
                        .toMaybe()
                        .doFinally(connection::close));
    }

    public Maybe<Clap> findByPostIdAndUserId(Long postId, Long userId) {
        Tuple arguments = Tuple.of(postId, userId);
        return find(ClapQuery.SELECT_BY_POST_AND_USER, arguments);
    }

    public Single<Long> saveByPostId(Long postId, Clap clap) {
        Tuple arguments = Tuple.of(
                clap.getPostId(),
                clap.getValue(),
                clap.getCreatedAt(),
                clap.getUpdatedAt(),
                clap.getCreatedBy(),
                clap.getUpdatedBy());
        return this.pool.rxGetConnection().flatMap(connection -> {

            Single<Long> single = connection.preparedQuery(ClapQuery.INSERT).rxExecute(arguments)
                    .flatMap(ignored -> connection.preparedQuery("select last_insert_id()").rxExecute())
                    .map(rows -> rows.iterator().next().getLong(0))
                    .flatMap(voteId -> connection.preparedQuery(ClapQuery.INSERT_CLAP_POST).rxExecute(Tuple.of(voteId, postId))
                            .map(ignored -> voteId));

            return connection.rxBegin().flatMap(transaction -> single
                    .doOnSuccess(ignored -> transaction.commit())
                    .doOnError(ignored -> transaction.rollback())
                    .doFinally(connection::close));
        });
    }
}
