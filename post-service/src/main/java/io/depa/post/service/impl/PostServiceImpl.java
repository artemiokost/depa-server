package io.depa.post.service.impl;

import io.depa.common.exception.CustomException;
import io.depa.common.model.Tracking;
import io.depa.common.type.ActionType;
import io.depa.common.type.CategoryType;
import io.depa.common.type.RoleType;
import io.depa.common.util.AmazonS3Helper;
import io.depa.common.util.Comparators;
import io.depa.post.model.*;
import io.depa.post.reposirory.*;
import io.depa.post.service.PostService;
import io.depa.post.util.PostHelper;
import io.depa.user.model.User;
import io.depa.user.model.UserSummary;
import io.depa.user.service.UserService;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.MaybeHelper;
import io.vertx.reactivex.SingleHelper;

import java.util.Objects;
import java.util.stream.Collectors;

public class PostServiceImpl implements PostService {

    // Services
    private final io.depa.user.reactivex.service.UserService userService;
    // Repositories
    private final BookmarkRepository bookmarkRepository;
    private final ClapRepository clapRepository;
    private final PostRepository postRepository;
    private final OriginRepository originRepository;
    private final TagRepository tagRepository;

    public PostServiceImpl() {
        // Service initialization
        this.userService = UserService.createProxy();
        // Repository initialization
        this.clapRepository = new ClapRepository();
        this.bookmarkRepository = new BookmarkRepository();
        this.postRepository = new PostRepository();
        this.tagRepository = new TagRepository();
        this.originRepository = new OriginRepository();
    }

    @Override
    public PostService createBookmark(Long postId, Long contextUserId, Handler<AsyncResult<JsonObject>> handler) {
        Bookmark bookmark = Bookmark.createInstance(postId, contextUserId);
        bookmarkRepository.save(bookmark)
                .map(bookmark::setId)
                .map(JsonObject::mapFrom)
                .subscribe(SingleHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService createByCategoryId(Long categoryId, JsonObject jsonObject, Long contextUserId,
                                          Handler<AsyncResult<Void>> handler) {
        JsonArray tags = jsonObject.getJsonArray("tags");
        JsonObject origin = jsonObject.getJsonObject("origin");
        userService.rxGetUserSummaryByUserId(contextUserId)
                .map(UserSummary::new)
                .map(userSummary -> Post.createInstance(categoryId, jsonObject, userSummary))
                .flatMapMaybe(post -> !Objects.equals(post.getCategoryId(), CategoryType.DISCUSSION.getId()) ?
                        AmazonS3Helper.uploadImage(post.getUri(), post.getImageUrl()).map(post::setImageUrl) :
                        Maybe.just(post))
                .flatMapSingle(post -> postRepository.save(post).map(post::setId))
                .flatMap(post -> tags != null ?
                        tagRepository.saveAllByPostId(post.getId(), tags.stream()
                                .map(s -> (String) s)
                                .map(Tag::new)
                                .collect(Collectors.toList()))
                                .map(integers -> post) :
                        Single.just(post))
                .flatMap(post -> origin != null ?
                        originRepository.save(new Origin(origin))
                                .flatMapCompletable(originId -> postRepository.updateOriginById(post.getId(), originId))
                                .andThen(Single.just(post)) :
                        Single.just(post))
                .ignoreElement()
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService deleteBookmark(Long contextUserId, Handler<AsyncResult<Void>> handler) {
        bookmarkRepository.deleteByUserId(contextUserId).subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService deleteBookmarkById(Long bookmarkId, Long contextUserId, Handler<AsyncResult<Void>> handler) {
        Maybe<Bookmark> bookmarkMaybe = bookmarkRepository.findById(bookmarkId);
        Maybe<User> userMaybe = userService.rxGetById(contextUserId).map(User::new).toMaybe();
        Maybe.zip(bookmarkMaybe, userMaybe, (bookmark, user) -> bookmark.getUserId().equals(user.getId()))
                .flatMapCompletable(hasAuthority -> hasAuthority ?
                        bookmarkRepository.deleteById(bookmarkId) :
                        Completable.error(CustomException.INVALID_CONTEXT_USER))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService deleteById(Long postId, Long contextUserId, Handler<AsyncResult<Void>> handler) {
        userService.rxGetRoleListById(contextUserId).flatMapCompletable(roleList ->
                postRepository.findById(postId).flatMapCompletable(post ->
                        roleList.contains(RoleType.MODERATOR.name()) || Objects.equals(post.getCreatedBy(), contextUserId) ?
                                AmazonS3Helper.deleteObject(post.getImageUrl()).andThen(postRepository.deleteById(postId)) :
                                Completable.error(CustomException.INVALID_CONTEXT_USER)))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getBookmarkList(Integer size, Long contextUserId, Handler<AsyncResult<JsonArray>> handler) {
        bookmarkRepository.findByUserId(size, contextUserId)
                .map(JsonArray::new)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getById(Long postId, Handler<AsyncResult<Post>> handler) {
        postRepository.findById(postId).subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getByUri(String uri, Handler<AsyncResult<JsonObject>> handler) {
        postRepository.findByUri(uri)
                .flatMap(PostHelper::zipWithExtra)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getByUriWithTracking(String uri, Long contextUserId, Handler<AsyncResult<JsonObject>> handler) {
        Tracking tracking = Tracking.createInstance(contextUserId, ActionType.READ.getId(), null);
        postRepository.findByUri(uri)
                .doAfterSuccess(post -> postRepository.saveTracking(tracking.setEntityId(post.getId())).subscribe())
                .flatMap(PostHelper::zipWithExtra)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getNeighboursById(Long postId, Handler<AsyncResult<JsonArray>> handler) {
        postRepository.findNeighboursById(postId)
                .toObservable()
                .flatMapIterable(e -> e)
                .map(post -> JsonObject.mapFrom(post).put("content", new JsonObject(post.getContent())))
                .toSortedList(Comparators.createdAtDesc())
                .map(JsonArray::new)
                .subscribe(SingleHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getPageByBookmark(Integer number, Integer size, Boolean value, Long contextUserId,
                                         Handler<AsyncResult<JsonObject>> handler) {
        postRepository.findPageByBookmark(number, size, value, contextUserId)
                .flatMap(PostHelper::mapPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getPageByCategoryId(Integer number, Integer size, Long categoryId,
                                           Handler<AsyncResult<JsonObject>> handler) {
        postRepository.findPageByCategory(number, size, categoryId)
                .flatMap(PostHelper::mapPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getPageByCategoryIdAndTargeting(Integer number, Integer size, Long categoryId, Long contextUserId,
                                                       Handler<AsyncResult<JsonObject>> handler) {
        postRepository.findPageByCategoryAndTargeting(number, size, categoryId, contextUserId)
                .flatMap(PostHelper::mapPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getPageByCategoryIdAndTagId(Integer number, Integer size, Long categoryId, Long tagId,
                                                   Handler<AsyncResult<JsonObject>> handler) {
        postRepository.findPageByCategoryAndTag(number, size, categoryId, tagId)
                .flatMap(PostHelper::mapPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getPageByMatch(Integer number, Integer size, String searchKey,
                                      Handler<AsyncResult<JsonObject>> handler) {
        postRepository.findPageByMatch(number, size, searchKey)
                .flatMap(PostHelper::mapPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getPageByPending(Integer number, Integer size, Boolean value,
                                        Handler<AsyncResult<JsonObject>> handler) {
        postRepository.findPageByPending(number, size, value)
                .flatMap(PostHelper::mapPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getPageByTagId(Integer number, Integer size, Long tagId,
                                      Handler<AsyncResult<JsonObject>> handler) {
        postRepository.findPageByTag(number, size, tagId)
                .flatMap(PostHelper::mapPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getPageByUserId(Integer number, Integer size, Long userId,
                                       Handler<AsyncResult<JsonObject>> handler) {
        postRepository.findPageByUser(number, size, userId)
                .flatMap(PostHelper::mapPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getTagById(Long tagId, Handler<AsyncResult<JsonObject>> handler) {
        tagRepository.findById(tagId).map(JsonObject::mapFrom).subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService getTagPageByMatch(Integer number, Integer size, String searchKey,
                                         Handler<AsyncResult<JsonObject>> handler) {
        tagRepository.findPageByMatch(number, size, searchKey)
                .map(JsonObject::mapFrom)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService updateClapByPostId(Long postId, Integer value, Long contextUserId,
                                          Handler<AsyncResult<Void>> handler) {
        clapRepository.findByPostIdAndUserId(postId, contextUserId)
                .switchIfEmpty(clapRepository.saveByPostId(postId, Clap.createInstance(postId, contextUserId, value))
                        .ignoreElement()
                        .andThen(Maybe.empty()))
                .flatMapCompletable(clap ->
                        clapRepository.updateById(clap.getId(), new Clap().setValue(clap.getValue() + value)))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService updateById(Long postId, JsonObject jsonObject, Long contextUserId,
                                  Handler<AsyncResult<Void>> handler) {
        Post newPost = new Post(jsonObject);
        if (newPost.getContent() != null) newPost.setBody(Post.extractBody(newPost.getContent()));
        userService.rxGetRoleListById(contextUserId)
                .doOnSuccess(ignored -> newPost.setUpdatedBy(contextUserId))
                .flatMap(roleList -> postRepository.findById(postId).flatMapSingle(post ->
                        roleList.contains(RoleType.MODERATOR.name()) || Objects.equals(post.getCreatedBy(), contextUserId) ?
                                Single.just(post) :
                                Single.error(CustomException.INVALID_CONTEXT_USER)))
                .flatMapCompletable(fileName ->
                        AmazonS3Helper.uploadImage("post/" + fileName, newPost.getImageUrl())
                                .doOnSuccess(newPost::setImageUrl)
                                .ignoreElement()
                                .andThen(postRepository.updateById(postId, newPost)))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService updatePendingById(Long postId, Boolean value, Handler<AsyncResult<Void>> handler) {
        postRepository.updatePendingById(postId, value).subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public PostService test(String image, Handler<AsyncResult<JsonObject>> handler) {
        AmazonS3Helper.uploadImage("hello", image)
                .map(JsonObject::mapFrom)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }
}
