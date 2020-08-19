package io.depa.post.reposirory;

import io.depa.common.data.Page;
import io.depa.common.repository.impl.AsyncRepository;
import io.depa.post.model.Comment;
import io.depa.post.query.CommentQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class CommentRepository extends AsyncRepository<Comment> {

    public CommentRepository() {
        super(Comment.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(CommentQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long commentId) {
        Tuple arguments = Tuple.of(commentId);
        return delete(CommentQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Comment>> findAll() {
        return findAll(CommentQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Comment> findById(Long commentId) {
        Tuple arguments = Tuple.of(commentId);
        return find(CommentQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Comment comment) {
        Tuple arguments = Arguments.builder()
                .add(comment.getPostId())
                .add(comment.getContent())
                .add(comment.getCreatedAt())
                .add(comment.getUpdatedAt())
                .add(comment.getCreatedBy())
                .add(comment.getUpdatedBy())
                .build();
        return save(CommentQuery.INSERT, arguments);
    }

    @Override
    public Completable updateById(Long commentId, Comment newComment) {
        return findById(commentId)
                .map(old -> old.merge(newComment))
                .map(merged -> Arguments.builder()
                        .add(merged.getContent())
                        .add(merged.getUpdatedAt())
                        .add(merged.getUpdatedBy())
                        .add(merged.getId())
                        .build())
                .flatMapCompletable(arguments -> update(CommentQuery.UPDATE_BY_ID, arguments));
    }

    public Maybe<List<Comment>> findAll(Integer size) {
        Tuple arguments = Tuple.of(size);
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(CommentQuery.SELECT_ALL_LIMIT).rxExecute(arguments)
                        .filter(rows -> rows.size() != 0)
                        .toObservable()
                        .flatMapIterable(e -> e)
                        .map(this::mapRowToJson)
                        .map(Comment::new)
                        .toList()
                        .toMaybe()
                        .doFinally(connection::close));
    }

    public Single<Integer> findCountByPost(Long postId) {
        Tuple arguments = Tuple.of(postId);
        return this.pool.rxGetConnection().flatMap(connection ->
                connection.preparedQuery(CommentQuery.SELECT_COUNT_BY_POST).rxExecute(arguments)
                        .map(rows -> rows.iterator().next().getInteger("count"))
                        .doFinally(connection::close));
    }

    public Maybe<Page<Comment>> findPageByPost(Integer number, Integer size, Long postId) {
        Tuple arguments = Arguments.builder()
                .add(postId)
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(CommentQuery.SELECT_BY_POST, arguments);
    }
}
