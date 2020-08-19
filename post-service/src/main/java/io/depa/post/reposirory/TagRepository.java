package io.depa.post.reposirory;

import io.depa.common.data.Page;
import io.depa.common.repository.impl.AsyncRepository;
import io.depa.post.model.Tag;
import io.depa.post.query.TagQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class TagRepository extends AsyncRepository<Tag> {

    public TagRepository() {
        super(Tag.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(TagQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long tagId) {
        Tuple arguments = Tuple.of(tagId);
        return delete(TagQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Tag>> findAll() {
        return findAll(TagQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Tag> findById(Long tagId) {
        Tuple arguments = Tuple.of(tagId);
        return find(TagQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Tag tag) {
        return save(TagQuery.INSERT, Tuple.of(tag.getName()));
    }

    @Override
    public Completable updateById(Long tagId, Tag newTag) {
        return findById(tagId)
                .map(old -> old.merge(newTag))
                .map(merged -> Tuple.of(merged.getName(), merged.getId()))
                .flatMapCompletable(arguments -> update(TagQuery.UPDATE_BY_ID, arguments));
    }

    public Maybe<Tag> findByName(String name) {
        Tuple arguments = Tuple.of(name);
        return find(TagQuery.SELECT_BY_NAME, arguments);
    }

    public Maybe<List<Tag>> findByPostId(Long postId) {
        Tuple arguments = Tuple.of(postId);
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(TagQuery.SELECT_BY_POST_ID).rxExecute(arguments)
                        .filter(rows -> rows.size() != 0)
                        .toObservable()
                        .flatMapIterable(e -> e)
                        .map(this::mapRowToJson)
                        .map(Tag::new)
                        .toList()
                        .toMaybe()
                        .doFinally(connection::close));
    }

    public Maybe<Page<Tag>> findPageByMatch(Integer number, Integer size, String searchKey) {
        Tuple arguments = Arguments.builder()
                .add(searchKey + '%')
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(TagQuery.SELECT_BY_MATCH, arguments);
    }

    public Single<List<Long>> saveAllByPostId(Long postId, List<Tag> tags) {
        return this.pool.rxGetConnection().flatMap(connection -> {

            Single<List<Long>> single = Observable.fromIterable(tags)
                    .map(Tag::getName)
                    .flatMapMaybe(this::findByName)
                    .toList()
                    .doOnSuccess(tags::removeAll)
                    .flatMapObservable(Observable::fromIterable)
                    .flatMapSingle(tag -> connection.preparedQuery(TagQuery.INSERT_TAG_POST).rxExecute(Tuple.of(tag.getId(), postId)))
                    .ignoreElements()
                    .andThen(Observable.fromIterable(tags))
                    .flatMapSingle(tag -> saveByPostId(postId, tag))
                    .toList();

            return connection.rxBegin().flatMap(transaction -> single
                    .doOnSuccess(ignored -> transaction.commit())
                    .doOnError(ignored -> transaction.rollback())
                    .doFinally(connection::close));
        });
    }

    public Single<Long> saveByPostId(Long postId, Tag tag) {
        return this.pool.rxGetConnection().flatMap(connection -> {

            Single<Long> single = connection.preparedQuery(TagQuery.INSERT).rxExecute(Tuple.of(tag.getName()))
                    .ignoreElement()
                    .andThen(connection.preparedQuery("select last_insert_id()").rxExecute())
                    .map(rows -> rows.iterator().next().getLong(0))
                    .flatMap(tagId -> connection.preparedQuery(TagQuery.INSERT_TAG_POST).rxExecute(Tuple.of(tagId, postId))
                            .ignoreElement()
                            .andThen(Single.just(tagId)));

            return connection.rxBegin().flatMap(transaction -> single
                    .doOnSuccess(ignored -> transaction.commit())
                    .doOnError(ignored -> transaction.rollback())
                    .doFinally(connection::close));
        });
    }
}
