package io.depa.post.reposirory;

import io.depa.common.repository.impl.AsyncRepository;
import io.depa.post.model.Bookmark;
import io.depa.post.query.BookmarkQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class BookmarkRepository extends AsyncRepository<Bookmark> {

    public BookmarkRepository() {
        super(Bookmark.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(BookmarkQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long bookmarkId) {
        Tuple arguments = Tuple.of(bookmarkId);
        return delete(BookmarkQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Bookmark>> findAll() {
        return findAll(BookmarkQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Bookmark> findById(Long bookmarkId) {
        Tuple arguments = Tuple.of(bookmarkId);
        return find(BookmarkQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Bookmark bookmark) {
        Tuple arguments = Arguments.builder()
                .add(bookmark.getPostId())
                .add(bookmark.getUserId())
                .add(bookmark.getCreatedAt())
                .add(bookmark.getUpdatedAt())
                .build();
        return save(BookmarkQuery.INSERT, arguments);
    }

    @Override
    public Completable updateById(Long subscriptionId, Bookmark newBookmark) {
        return findById(subscriptionId)
                .map(old -> old.merge(newBookmark))
                .map(merged -> Arguments.builder()
                        .add(merged.getPostId())
                        .add(merged.getUserId())
                        .add(merged.getUpdatedAt())
                        .add(merged.getId())
                        .build())
                .flatMapCompletable(arguments -> update(BookmarkQuery.UPDATE_BY_ID, arguments));
    }

    public Completable deleteByUserId(Long userId) {
        Tuple arguments = Tuple.of(userId);
        return delete(BookmarkQuery.DELETE_BY_USER, arguments);
    }

    public Maybe<List<Bookmark>> findByUserId(Integer size, Long userId) {
        Tuple arguments = Tuple.of(userId, size);
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(BookmarkQuery.SELECT_BY_USER_LIMIT).rxExecute(arguments)
                        .filter(rows -> rows.size() != 0)
                        .toObservable()
                        .flatMapIterable(e -> e)
                        .map(this::mapRowToJson)
                        .map(Bookmark::new)
                        .toList()
                        .toMaybe()
                        .doFinally(connection::close));
    }
}
