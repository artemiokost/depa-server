package io.depa.post;

import io.depa.common.RestfulVerticle;
import io.depa.common.type.CategoryType;
import io.depa.common.type.RoleType;
import io.depa.common.util.Helper;
import io.depa.common.util.Runner;
import io.depa.post.service.CommentService;
import io.depa.post.service.PostService;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class PostVerticle extends RestfulVerticle {

    private static final Integer DEFAULT_PORT = 8002;

    private CommentService commentService;
    private PostService postService;

    public static void main(String[] args) {
        Runner.run(PostVerticle.class, PostService.NAME);
    }

    @Override
    public void start() {

        Integer port = config().getInteger("http.port", DEFAULT_PORT);
        String host = config().getString("http.host", localHost.getHostAddress());
        String startInfo = "Service <" + PostService.NAME + "> start at port: " + port;

        commentService = CommentService.create();
        postService = PostService.create();

        OpenAPI3RouterFactory.rxCreate(vertx, config().getString("open.api")).subscribe(factory -> {

            factory.addSecurityHandler("bearerAuth", JWTAuthHandler.create(createJWTAuth(vertx)));

            // Static operations:
            factory.addHandlerByOperationId("getStatic", StaticHandler.create());
            // Comment operations:
            factory.addHandlerByOperationId("createCommentByPostId", this::createCommentByPostId);
            factory.addHandlerByOperationId("createCommentVoteByCommentId", this::createCommentVoteByCommentId);
            factory.addHandlerByOperationId("deleteCommentById", this::deleteCommentById);
            factory.addHandlerByOperationId("deleteCommentVoteById", this::deleteCommentVoteById);
            factory.addHandlerByOperationId("getCommentList", this::getCommentList);
            factory.addHandlerByOperationId("getCommentPageByPostId", this::getCommentPageByPostId);
            factory.addHandlerByOperationId("updateCommentVoteById", this::updateCommentVoteById);
            // Post operations:
            factory.addHandlerByOperationId("createBookmark", this::createBookmark);
            factory.addHandlerByOperationId("createByCategoryId", this::createByCategoryId);
            factory.addHandlerByOperationId("deleteBookmark", this::deleteBookmark);
            factory.addHandlerByOperationId("deleteBookmarkById", this::deleteBookmarkById);
            factory.addHandlerByOperationId("deleteById", this::deleteById);
            factory.addHandlerByOperationId("getBookmarkList", this::getBookmarkList);
            factory.addHandlerByOperationId("getByUri", this::getByUri);
            factory.addHandlerByOperationId("getByUriWithTracking", this::getByUriWithTracking);
            factory.addHandlerByOperationId("getNeighboursById", this::getNeighboursById);
            factory.addHandlerByOperationId("getPageByBookmark", this::getPageByBookmark);
            factory.addHandlerByOperationId("getPageByCategoryId", this::getPageByCategoryId);
            factory.addHandlerByOperationId("getPageByCategoryIdAndTargeting", this::getPageByCategoryIdAndTargeting);
            factory.addHandlerByOperationId("getPageByCategoryIdAndTagId", this::getPageByCategoryIdAndTagId);
            factory.addHandlerByOperationId("getPageByMatch", this::getPageByMatch);
            factory.addHandlerByOperationId("getPageByPending", this::getPageByPending);
            factory.addHandlerByOperationId("getPageByTagId", this::getPageByTagId);
            factory.addHandlerByOperationId("getPageByUserId", this::getPageByUserId);
            factory.addHandlerByOperationId("getTagById", this::getTagById);
            factory.addHandlerByOperationId("getTagPageByMatch", this::getTagPageByMatch);
            factory.addHandlerByOperationId("updateClapByPostId", this::updateClapByPostId);
            factory.addHandlerByOperationId("updateById", this::updateById);
            factory.addHandlerByOperationId("updatePendingById", this::updatePendingById);
            factory.addHandlerByOperationId("test", this::test);

            vertx.createHttpServer()
                    .requestHandler(factory.getRouter())
                    .rxListen(port, host)
                    .ignoreElement()
                    .andThen(publishEventBusService(CommentService.NAME, CommentService.ADDRESS, CommentService.class.getName()))
                    .andThen(publishEventBusService(PostService.NAME, PostService.ADDRESS, PostService.class.getName()))
                    .andThen(publishHttpEndpoint(CommentService.NAME, host, port))
                    .andThen(publishHttpEndpoint(PostService.NAME, host, port))
                    .andThen(registerHandler(CommentService.ADDRESS, CommentService.class, commentService))
                    .andThen(registerHandler(PostService.ADDRESS, PostService.class, postService))
                    .subscribe(() -> log.info(startInfo), e -> log.error(e.getMessage()));
            }, e -> log.error(e.getMessage()));
    }

    // Comment handlers:
    private void createCommentByPostId(RoutingContext context) {
        Long postId = Long.parseLong(context.pathParam("postId"));
        Long contextUserId = context.user().principal().getLong("userId");
        commentService.createCommentByPostId(postId, context.getBodyAsJson(), contextUserId, resultVoidHandler(context));
    }

    private void createCommentVoteByCommentId(RoutingContext context) {
        Long commentId = Long.parseLong(context.pathParam("commentId"));
        Boolean value = Boolean.parseBoolean(context.pathParam("value"));
        Long contextUserId = context.user().principal().getLong("userId");
        commentService.createCommentVoteByCommentId(commentId, value, contextUserId, resultHandler(context));
    }

    private void deleteCommentById(RoutingContext context) {
        Long commentId = Long.parseLong(context.pathParam("commentId"));
        Long contextUserId = context.user().principal().getLong("userId");
        commentService.deleteCommentById(commentId, contextUserId, resultVoidHandler(context));
    }

    private void deleteCommentVoteById(RoutingContext context) {
        Long voteId = Long.parseLong(context.pathParam("voteId"));
        Long contextUserId = context.user().principal().getLong("userId");
        commentService.deleteCommentVoteById(voteId, contextUserId, resultVoidHandler(context));
    }

    private void getCommentList(RoutingContext context) {
        Integer size = Integer.parseInt(context.pathParam("size"));
        commentService.getCommentList(size, resultHandler(context));
    }

    private void getCommentPageByPostId(RoutingContext context) {
        Long postId = Long.parseLong(context.pathParam("postId"));
        Helper.mapPageParams(context, (number, size) ->
                commentService.getCommentPageByPostId(number, size, postId, resultHandler(context)));
    }

    private void updateCommentVoteById(RoutingContext context) {
        Long voteId = Long.parseLong(context.pathParam("voteId"));
        Boolean value = Boolean.parseBoolean(context.pathParam("value"));
        Long contextUserId = context.user().principal().getLong("userId");
        Helper.mapPageParams(context, (number, size) ->
                commentService.updateCommentVoteById(voteId, value, contextUserId, resultHandler(context)));
    }

    // Post handlers:
    private void createBookmark(RoutingContext context) {
        Long postId = Long.parseLong(context.request().getParam("postId"));
        Long contextUserId = context.user().principal().getLong("userId");
        postService.createBookmark(postId, contextUserId, resultHandler(context));
    }

    private void createByCategoryId(RoutingContext context) {
        Long categoryId = Long.parseLong(context.request().getParam("categoryId"));
        Long contextUserId = context.user().principal().getLong("userId");
        JsonObject bodyAsJson = context.getBodyAsJson();
        if (Objects.equals(categoryId, CategoryType.BLOG.getId()) || Objects.equals(categoryId, CategoryType.DISCUSSION.getId())) {
            postService.createByCategoryId(categoryId, context.getBodyAsJson(), contextUserId, resultVoidHandler(context));
        } else {
            hasRole(RoleType.CONTRIBUTOR.name(), context.user())
                    .doOnComplete(() -> postService.createByCategoryId(categoryId, bodyAsJson, contextUserId, resultVoidHandler(context)))
                    .doOnError(e -> unauthorized(context, e))
                    .subscribe();
        }
    }

    private void deleteBookmark(RoutingContext context) {
        Long contextUserId = context.user().principal().getLong("userId");
        postService.deleteBookmark(contextUserId, resultVoidHandler(context));
    }

    private void deleteBookmarkById(RoutingContext context) {
        Long bookmarkId = Long.parseLong(context.pathParam("bookmarkId"));
        Long contextUserId = context.user().principal().getLong("userId");
        postService.deleteBookmarkById(bookmarkId, contextUserId, resultVoidHandler(context));
    }

    private void deleteById(RoutingContext context) {
        Long postId = Long.parseLong(context.pathParam("postId"));
        Long contextUserId = context.user().principal().getLong("userId");
        postService.deleteById(postId, contextUserId, resultVoidHandler(context));
    }

    private void getBookmarkList(RoutingContext context) {
        Integer size = Integer.parseInt(context.pathParam("size"));
        Long contextUserId = context.user().principal().getLong("userId");
        postService.getBookmarkList(size, contextUserId, resultHandler(context));
    }

    private void getByUri(RoutingContext context) {
        String uri = context.pathParam("uri");
        postService.getByUri(uri, resultHandler(context));
    }

    private void getByUriWithTracking(RoutingContext context) {
        String uri = context.pathParam("uri");
        Long contextUserId = context.user().principal().getLong("userId");
        postService.getByUriWithTracking(uri, contextUserId, resultHandler(context));
    }

    private void getNeighboursById(RoutingContext context) {
        Long postId = Long.parseLong(context.pathParam("postId"));
        postService.getNeighboursById(postId, resultHandler(context));
    }

    private void getPageByBookmark(RoutingContext context) {
        Boolean value = Boolean.parseBoolean(context.pathParam("value"));
        Long contextUserId = context.user().principal().getLong("userId");
        Helper.mapPageParams(context, (number, size) ->
                postService.getPageByBookmark(number, size, value, contextUserId, resultHandler(context)));
    }

    private void getPageByCategoryId(RoutingContext context) {
        Long categoryId = Long.parseLong(context.pathParam("categoryId"));
        Helper.mapPageParams(context, (number, size) ->
                postService.getPageByCategoryId(number, size, categoryId, resultHandler(context)));
    }

    private void getPageByCategoryIdAndTargeting(RoutingContext context) {
        Long categoryId = Long.parseLong(context.pathParam("categoryId"));
        Long contextUserId = context.user().principal().getLong("userId");
        Helper.mapPageParams(context, (number, size) ->
                postService.getPageByCategoryIdAndTargeting(number, size, categoryId, contextUserId, resultHandler(context)));
    }

    private void getPageByCategoryIdAndTagId(RoutingContext context) {
        Long categoryId = Long.parseLong(context.pathParam("categoryId"));
        Long tagId = Long.parseLong(context.request().getParam("tagId"));
        Helper.mapPageParams(context, (number, size) ->
                postService.getPageByCategoryIdAndTagId(number, size, categoryId, tagId, resultHandler(context)));
    }

    private void getPageByMatch(RoutingContext context) {
        String searchKey = context.pathParam("searchKey");
        Helper.mapPageParams(context, (number, size) ->
                postService.getPageByMatch(number, size, searchKey, resultHandler(context)));
    }

    private void getPageByPending(RoutingContext context) {
        Boolean value = Boolean.parseBoolean(context.pathParam("value"));
        Helper.mapPageParams(context, (number, size) ->
                postService.getPageByPending(number, size, value, resultHandler(context)));
    }

    private void getPageByTagId(RoutingContext context) {
        Long tagId = Long.parseLong(context.pathParam("tagId"));
        Helper.mapPageParams(context, (number, size) ->
                postService.getPageByTagId(number, size, tagId, resultHandler(context)));
    }

    private void getPageByUserId(RoutingContext context) {
        Long userId = Long.parseLong(context.pathParam("userId"));
        Helper.mapPageParams(context, (number, size) ->
                postService.getPageByUserId(number, size, userId, resultHandler(context)));
    }

    private void getTagById(RoutingContext context) {
        Long tagId = Long.parseLong(context.pathParam("tagId"));
        postService.getTagById(tagId, resultHandler(context));
    }

    private void getTagPageByMatch(RoutingContext context) {
        String searchKey = context.pathParam("searchKey");
        Helper.mapPageParams(context, (number, size) ->
                postService.getTagPageByMatch(number, size, searchKey, resultHandler(context)));
    }

    private void updateClapByPostId(RoutingContext context) {
        Long postId = Long.parseLong(context.pathParam("postId"));
        Integer value = Integer.parseInt(context.pathParam("value"));
        Long contextUserId = context.user().principal().getLong("userId");
        postService.updateClapByPostId(postId, value, contextUserId, resultVoidHandler(context));
    }

    private void updateById(RoutingContext context) {
        Long postId = Long.parseLong(context.pathParam("postId"));
        Long contextUserId = context.user().principal().getLong("userId");
        postService.updateById(postId, context.getBodyAsJson(), contextUserId, resultVoidHandler(context));
    }

    private void updatePendingById(RoutingContext context) {
        Long postId = Long.parseLong(context.pathParam("postId"));
        Boolean value = Boolean.parseBoolean(context.pathParam("value"));
        postService.updatePendingById(postId, value, resultVoidHandler(context));
    }

    private void test(RoutingContext context) {
        String image = context.getBodyAsJson().getString("image");
        postService.test(image, resultHandler(context));
    }
}
