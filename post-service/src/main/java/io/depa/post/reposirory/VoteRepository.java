package io.depa.post.reposirory;

import io.depa.common.repository.impl.AsyncRepository;
import io.depa.post.model.Vote;
import io.depa.post.query.VoteQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class VoteRepository extends AsyncRepository<Vote> {

    public VoteRepository() {
        super(Vote.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(VoteQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long voteId) {
        Tuple arguments = Tuple.of(voteId);
        return delete(VoteQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Vote>> findAll() {
        return findAll(VoteQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Vote> findById(Long voteId) {
        Tuple arguments = Tuple.of(voteId);
        return find(VoteQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Vote vote) {
        Tuple arguments = Arguments.builder()
                .add(vote.getCommentId())
                .add(vote.getValue())
                .add(vote.getCreatedAt())
                .add(vote.getUpdatedAt())
                .add(vote.getCreatedBy())
                .add(vote.getUpdatedBy())
                .build();
        return save(VoteQuery.INSERT, arguments);
    }

    @Override
    public Completable updateById(Long voteId, Vote newVote) {
        return findById(voteId)
                .map(old -> old.merge(newVote))
                .map(merged -> Arguments.builder()
                        .add(merged.getValue())
                        .add(merged.getUpdatedAt())
                        .add(merged.getUpdatedBy())
                        .add(merged.getId())
                        .build())
                .flatMapCompletable(arguments -> update(VoteQuery.UPDATE_BY_ID, arguments));
    }

    public Maybe<List<Vote>> findByCommentId(Long commentId) {
        Tuple arguments = Tuple.of(commentId);
        return this.pool.rxGetConnection().flatMapMaybe(connection ->
                connection.preparedQuery(VoteQuery.SELECT_BY_COMMENT).rxExecute(arguments)
                        .filter(rows -> rows.size() != 0)
                        .toObservable()
                        .flatMapIterable(e -> e)
                        .map(this::mapRowToJson)
                        .map(Vote::new)
                        .toList()
                        .toMaybe()
                        .doFinally(connection::close));
    }
}
