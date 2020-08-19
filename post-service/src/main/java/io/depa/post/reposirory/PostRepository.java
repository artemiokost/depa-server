package io.depa.post.reposirory;

import io.depa.common.data.Page;
import io.depa.common.model.Tracking;
import io.depa.common.repository.impl.AsyncRepository;
import io.depa.post.model.Post;
import io.depa.post.query.PostQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class PostRepository extends AsyncRepository<Post> {

    public PostRepository() {
        super(Post.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(PostQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long postId) {
        Tuple arguments = Tuple.of(postId);
        return delete(PostQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Post>> findAll() {
        return findAll(PostQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Post> findById(Long postId) {
        Tuple arguments = Tuple.of(postId);
        return find(PostQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Post post) {
        Tuple arguments = Arguments.builder()
                .add(post.getCategoryId())
                .add(post.getPending())
                .add(post.getBody())
                .add(post.getContent())
                .add(post.getImageUrl())
                .add(post.getTitle())
                .add(post.getUri())
                .add(post.getCreatedAt())
                .add(post.getUpdatedAt())
                .add(post.getCreatedBy())
                .add(post.getUpdatedBy())
                .build();
        return save(PostQuery.INSERT, arguments);
    }

    @Override
    public Completable updateById(Long postId, Post newPost) {
        return findById(postId)
                .map(old -> old.merge(newPost))
                .map(merged -> Arguments.builder()
                        .add(merged.getCategoryId())
                        .add(merged.getBody())
                        .add(merged.getContent())
                        .add(merged.getImageUrl())
                        .add(merged.getTitle())
                        .add(merged.getUri())
                        .add(merged.getUpdatedAt())
                        .add(merged.getUpdatedBy())
                        .add(merged.getId())
                        .build())
                .flatMapCompletable(arguments -> update(PostQuery.UPDATE_BY_ID, arguments));
    }

    public Maybe<Page<Post>> findPageByBookmark(Integer number, Integer size, Boolean value, Long userId) {
        Tuple arguments = Tuple.of(userId, calcPage(number, size), size);
        String statement = value ? PostQuery.SELECT_BY_BOOKMARK : PostQuery.SELECT_BY_NOT_BOOKMARK;
        return findPage(statement, arguments);
    }

    public Maybe<Post> findByUri(String uri) {
        Tuple arguments = Tuple.of(uri);
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(PostQuery.SELECT_BY_URI).rxExecute(arguments)
                        .filter(rows -> rows.size() != 0)
                        .map(rows -> mapRowToJson(rows.iterator().next()))
                        .map(Post::new)
                        .doOnSuccess(post -> updateViewsById(post.getId(), post.getViews() + 1).subscribe())
                        .doFinally(connection::close));
    }

    public Completable saveTracking(Tracking tracking) {
        Tuple arguments = Arguments.builder()
                .add(tracking.getActionId())
                .add(tracking.getEntityId())
                .add(tracking.getCreatedAt())
                .add(tracking.getUpdatedAt())
                .add(tracking.getCreatedBy())
                .add(tracking.getUpdatedBy())
                .build();
        return save(PostQuery.INSERT_TRACKING_POST, arguments).ignoreElement();
    }

    public Maybe<List<Post>> findNeighboursById(Long postId) {
        Tuple arguments = Tuple.of(postId, postId);
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(PostQuery.SELECT_NEIGHBOURS_BY_ID).rxExecute(arguments)
                        .filter(rows -> rows.size() != 0)
                        .toObservable()
                        .flatMapIterable(e -> e)
                        .map(this::mapRowToJson)
                        .map(Post::new)
                        .toList()
                        .toMaybe()
                        .doFinally(connection::close));
    }

    public Maybe<Page<Post>> findPageByCategory(Integer number, Integer size, Long categoryId) {
        Tuple arguments = Arguments.builder()
                .add(categoryId)
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(PostQuery.SELECT_BY_CATEGORY, arguments);
    }

    public Maybe<Page<Post>> findPageByCategoryAndTargeting(Integer number, Integer size, Long categoryId, Long contextUserId) {
        Tuple arguments = Arguments.builder()
                .add(contextUserId)
                .add(categoryId)
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(PostQuery.SELECT_BY_CATEGORY_AND_TARGETING, arguments);
    }

    public Maybe<Page<Post>> findPageByCategoryAndTag(Integer number, Integer size, Long categoryId, Long tagId) {
        Tuple arguments = Arguments.builder()
                .add(categoryId)
                .add(tagId)
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(PostQuery.SELECT_BY_CATEGORY_AND_TAG, arguments);
    }

    public Maybe<Page<Post>> findPageByMatch(Integer number, Integer size, String searchKey) {
        Tuple arguments = Arguments.builder()
                .add(searchKey)
                .add(searchKey)
                .add(searchKey)
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(PostQuery.SELECT_BY_MATCH, arguments);
    }

    public Maybe<Page<Post>> findPageByPending(Integer number, Integer size, Boolean value) {
        Tuple arguments = Arguments.builder()
                .add(value)
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(PostQuery.SELECT_BY_PENDING, arguments);
    }

    public Maybe<Page<Post>> findPageByTag(Integer number, Integer size, Long tagId) {
        Tuple arguments = Arguments.builder()
                .add(tagId)
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(PostQuery.SELECT_BY_TAG, arguments);
    }

    public Maybe<Page<Post>> findPageByUser(Integer number, Integer size, Long userId) {
        Tuple arguments = Arguments.builder()
                .add(userId)
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(PostQuery.SELECT_BY_USER, arguments);
    }

    public Completable updateOriginById(Long postId, Long originId) {
        Tuple arguments = Tuple.of(originId, postId);
        return update(PostQuery.UPDATE_ORIGIN_BY_ID, arguments);
    }

    public Completable updatePendingById(Long postId, Boolean value) {
        Tuple arguments = Tuple.of(value, postId);
        return update(PostQuery.UPDATE_PENDING_BY_ID, arguments);
    }

    public Completable updateViewsById(Long postId, Integer value) {
        Tuple arguments = Tuple.of(value, postId);
        return update(PostQuery.UPDATE_VIEWS_BY_ID, arguments);
    }
}
