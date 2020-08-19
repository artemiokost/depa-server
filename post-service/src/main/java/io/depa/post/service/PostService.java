package io.depa.post.service;

import io.depa.common.context.ApplicationContext;
import io.depa.post.model.Post;
import io.depa.post.service.impl.PostServiceImpl;
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
public interface PostService {

    String ADDRESS = "service.post";
    String NAME = "post-service";

    @GenIgnore
    static PostService create() {
        return new PostServiceImpl();
    }

    @GenIgnore
    static io.depa.post.reactivex.service.PostService createProxy() {
        io.vertx.core.Vertx delegate = ApplicationContext.getVertx().getDelegate();
        PostServiceVertxEBProxy proxy = new PostServiceVertxEBProxy(delegate, ADDRESS);
        return new io.depa.post.reactivex.service.PostService(proxy);
    }

    @Fluent
    PostService createBookmark(Long postId, Long contextUserId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService createByCategoryId(Long categoryId, JsonObject jsonObject, Long contextUserId,
                                   Handler<AsyncResult<Void>> handler);

    @Fluent
    PostService deleteBookmark(Long contextUserId, Handler<AsyncResult<Void>> handler);

    @Fluent
    PostService deleteBookmarkById(Long bookmarkId, Long contextUserId, Handler<AsyncResult<Void>> handler);

    @Fluent
    PostService deleteById(Long postId, Long contextUserId, Handler<AsyncResult<Void>> handler);

    @Fluent
    PostService getBookmarkList(Integer size, Long contextUserId, Handler<AsyncResult<JsonArray>> handler);

    @Fluent
    PostService getById(Long postId, Handler<AsyncResult<Post>> handler);

    @Fluent
    PostService getByUri(String uri, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getByUriWithTracking(String uri, Long contextUserId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getNeighboursById(Long postId, Handler<AsyncResult<JsonArray>> handler);

    @Fluent
    PostService getPageByBookmark(Integer number, Integer size, Boolean value, Long contextUserId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getPageByCategoryId(Integer number, Integer size, Long categoryId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getPageByCategoryIdAndTargeting(Integer number, Integer size, Long categoryId, Long contextUserId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getPageByCategoryIdAndTagId(Integer number, Integer size, Long categoryId, Long tagId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getPageByMatch(Integer number, Integer size, String searchKey, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getPageByPending(Integer number, Integer size, Boolean value, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getPageByTagId(Integer number, Integer size, Long tagId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getPageByUserId(Integer number, Integer size, Long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getTagById(Long tagId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService getTagPageByMatch(Integer number, Integer size, String searchKey, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    PostService updateClapByPostId(Long postId, Integer count, Long contextUserId, Handler<AsyncResult<Void>> handler);

    @Fluent
    PostService updateById(Long postId, JsonObject jsonObject, Long contextUserId, Handler<AsyncResult<Void>> handler);

    @Fluent
    PostService updatePendingById(Long postId, Boolean value, Handler<AsyncResult<Void>> handler);

    @Fluent
    PostService test(String base64image, Handler<AsyncResult<JsonObject>> handler);
}
