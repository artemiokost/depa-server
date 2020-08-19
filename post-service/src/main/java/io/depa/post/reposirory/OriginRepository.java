package io.depa.post.reposirory;

import io.depa.common.repository.impl.AsyncRepository;
import io.depa.post.model.Origin;
import io.depa.post.query.OriginQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class OriginRepository extends AsyncRepository<Origin> {


    public OriginRepository() {
        super(Origin.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(OriginQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long originId) {
        Tuple arguments = Tuple.of(originId);
        return delete(OriginQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Origin>> findAll() {
        return findAll(OriginQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Origin> findById(Long originId) {
        Tuple arguments = Tuple.of(originId);
        return find(OriginQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Origin origin) {
        Tuple arguments = Tuple.of(origin.getName(), origin.getUrl());
        return save(OriginQuery.INSERT_OR_UPDATE, arguments);
    }

    @Override
    public Completable updateById(Long originId, Origin newOrigin) {
        return findById(originId)
                .map(old -> old.merge(newOrigin))
                .map(merged -> Tuple.of(merged.getName(), merged.getUrl(), merged.getId()))
                .flatMapCompletable(arguments -> update(OriginQuery.UPDATE_BY_ID, arguments));
    }

    public Maybe<Origin> findByName(String name) {
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(OriginQuery.SELECT_BY_NAME).rxExecute(Tuple.of(name))
                        .filter(rows -> rows.size() != 0)
                        .map(rows -> mapRowToJson(rows.iterator().next()))
                        .map(Origin::new)
                        .doFinally(connection::close));
    }
}
