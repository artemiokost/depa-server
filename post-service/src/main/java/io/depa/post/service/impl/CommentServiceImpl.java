package io.depa.post.service.impl;

import io.depa.common.context.ApplicationContext;
import io.depa.common.event.Event;
import io.depa.common.event.action.CommentAction;
import io.depa.common.event.type.EventType;
import io.depa.common.exception.CustomException;
import io.depa.common.type.RoleType;
import io.depa.common.util.Comparators;
import io.depa.post.model.Comment;
import io.depa.post.model.Vote;
import io.depa.post.reposirory.CommentRepository;
import io.depa.post.reposirory.VoteRepository;
import io.depa.post.service.CommentService;
import io.depa.post.util.CommentHelper;
import io.depa.user.service.UserService;
import io.reactivex.Completable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.MaybeHelper;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.core.Vertx;

import java.util.Objects;

public class CommentServiceImpl implements CommentService {

    // Injections
    private final Vertx vertx;
    // Services
    private final io.depa.user.reactivex.service.UserService userService;
    // Repositories
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;

    public CommentServiceImpl() {
        // Injection initialization
        this.vertx = ApplicationContext.getVertx();
        // Service initialization
        this.userService = UserService.createProxy();
        // Repository initialization
        this.commentRepository = new CommentRepository();
        this.voteRepository = new VoteRepository();
    }

    @Override
    public CommentService createCommentByPostId(Long postId, JsonObject jsonObject, Long contextUserId,
                                                Handler<AsyncResult<Void>> handler) {
        commentRepository.save(Comment.createInstance(jsonObject, postId, contextUserId))
                .ignoreElement()
                .doOnComplete(() -> vertx.eventBus().publish(CommentService.NAME, Event.builder()
                        .action(CommentAction.CREATE_COMMENT)
                        .type(EventType.COMMENT)
                        .payload(new JsonObject().put("postId", postId))
                        .build()
                        .toJson()))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public CommentService createCommentVoteByCommentId(Long commentId, Boolean value, Long contextUserId,
                                                       Handler<AsyncResult<JsonObject>> handler) {
        Vote vote = Vote.createInstance(value, commentId, contextUserId);
        voteRepository.save(vote)
                .map(vote::setId)
                .map(JsonObject::mapFrom)
                .subscribe(SingleHelper.toObserver(handler));
        return this;
    }

    @Override
    public CommentService deleteCommentById(Long commentId, Long contextUserId,
                                            Handler<AsyncResult<Void>> handler) {
        userService.rxGetRoleListById(contextUserId).flatMapCompletable(roleList ->
                commentRepository.findById(commentId).flatMapCompletable(comment ->
                        roleList.contains(RoleType.MODERATOR.name()) || Objects.equals(comment.getCreatedBy(), contextUserId) ?
                                commentRepository.deleteById(commentId) :
                                Completable.error(CustomException.INVALID_CONTEXT_USER)))
                .doOnComplete(() -> vertx.eventBus().publish(CommentService.NAME, Event.builder()
                        .action(CommentAction.DELETE_COMMENT)
                        .type(EventType.COMMENT)
                        .payload(new JsonObject().put("commentId", commentId))
                        .build()
                        .toJson()))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public CommentService deleteCommentVoteById(Long voteId, Long contextUserId,
                                                Handler<AsyncResult<Void>> handler) {
        voteRepository.findById(voteId).flatMapCompletable(vote ->
                Objects.equals(vote.getCreatedBy(), contextUserId) ?
                        voteRepository.deleteById(voteId) :
                        Completable.error(CustomException.INVALID_CONTEXT_USER))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public CommentService getCommentCountByPostId(Long postId, Handler<AsyncResult<Integer>> handler) {
        commentRepository.findCountByPost(postId).subscribe(SingleHelper.toObserver(handler));
        return this;
    }

    @Override
    public CommentService getCommentList(Integer size, Handler<AsyncResult<JsonArray>> handler) {
        commentRepository.findAll(size)
                .toObservable()
                .flatMapIterable(e -> e)
                .flatMapMaybe(CommentHelper::zipWithExtra2)
                .toSortedList(Comparators.createdAtDesc())
                .map(JsonArray::new)
                .subscribe(SingleHelper.toObserver(handler));
        return this;
    }

    @Override
    public CommentService getCommentPageByPostId(Integer number, Integer size, Long postId,
                                                 Handler<AsyncResult<JsonObject>> handler) {
        commentRepository.findPageByPost(number, size, postId)
                .flatMap(CommentHelper::mapPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public CommentService updateCommentVoteById(Long voteId, Boolean value, Long contextUserId,
                                                Handler<AsyncResult<Void>> handler) {
        voteRepository.findById(voteId).flatMapCompletable(vote ->
                Objects.equals(vote.getCreatedBy(), contextUserId) ?
                        voteRepository.updateById(voteId, new Vote().setValue(value)) :
                        Completable.error(CustomException.INVALID_CONTEXT_USER))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }
}
