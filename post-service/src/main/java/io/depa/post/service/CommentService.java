package io.depa.post.service;

import io.depa.common.context.ApplicationContext;
import io.depa.post.service.impl.CommentServiceImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface CommentService {

    String ADDRESS = "service.comment";
    String NAME = "comment-service";

    @GenIgnore
    static CommentService create() {
        return new CommentServiceImpl();
    }

    @GenIgnore
    static io.depa.post.reactivex.service.CommentService createProxy() {
        io.vertx.core.Vertx delegate = ApplicationContext.getVertx().getDelegate();
        CommentServiceVertxEBProxy proxy = new CommentServiceVertxEBProxy(delegate, ADDRESS);
        return new io.depa.post.reactivex.service.CommentService(proxy);
    }

    @Fluent
    CommentService createCommentByPostId(Long postId, JsonObject jsonObject, Long contextUserId, Handler<AsyncResult<Void>> handler);

    @Fluent
    CommentService createCommentVoteByCommentId(Long commentId, Boolean value, Long contextUserId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    CommentService deleteCommentById(Long commentId, Long contextUserId, Handler<AsyncResult<Void>> handler);

    @Fluent
    CommentService deleteCommentVoteById(Long voteId, Long contextUserId, Handler<AsyncResult<Void>> handler);

    @Fluent
    CommentService getCommentCountByPostId(Long postId, Handler<AsyncResult<Integer>> handler);

    @Fluent
    CommentService getCommentList(Integer size, Handler<AsyncResult<JsonArray>> handler);

    @Fluent
    CommentService getCommentPageByPostId(Integer number, Integer size, Long postId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    CommentService updateCommentVoteById(Long voteId, Boolean value, Long contextUserId, Handler<AsyncResult<Void>> handler);
}
