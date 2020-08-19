package io.depa.post.util;

import io.depa.common.data.Page;
import io.depa.common.util.Comparators;
import io.depa.post.model.Comment;
import io.depa.post.model.Vote;
import io.depa.post.reposirory.VoteRepository;
import io.depa.post.service.PostService;
import io.depa.user.service.UserService;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class CommentHelper {

    // Repositories
    private static final VoteRepository voteRepository = new VoteRepository();
    // Services
    private static final io.depa.post.reactivex.service.PostService postService = PostService.createProxy();
    private static final io.depa.user.reactivex.service.UserService userService = UserService.createProxy();

    /**
     * This static method generates JsonObject that represents a comment page
     * with extra content.
     *
     * @return handler
     */
    public static Maybe<JsonObject> mapPageToJson(Page<Comment> commentPage) {
        return Observable.just(commentPage.getList())
                .flatMapIterable(e -> e)
                .flatMapMaybe(CommentHelper::zipWithExtra)
                .toSortedList(Comparators.createdAtDesc())
                .map(JsonArray::new)
                .map(jsonArray -> JsonObject.mapFrom(commentPage).put("list", jsonArray))
                .toMaybe();
    }

    public static Maybe<JsonObject> zipWithExtra(Comment comment) {

        Maybe<JsonObject> creatorSummaryMaybe = userService.rxGetUserSummaryByUserId(comment.getCreatedBy()).toMaybe();
        Maybe<List<Vote>> voteListMaybe = voteRepository.findByCommentId(comment.getId()).defaultIfEmpty(new ArrayList<>());

        return Maybe.zip(creatorSummaryMaybe, voteListMaybe, (creatorSummary, votes) ->
                JsonObject.mapFrom(comment)
                        .put("extra", new JsonObject()
                                .put("creatorSummary", creatorSummary)
                                .put("votes", votes)));
    }

    public static Maybe<JsonObject> zipWithExtra2(Comment comment) {

        Maybe<JsonObject> creatorSummaryMaybe = userService.rxGetUserSummaryByUserId(comment.getCreatedBy()).toMaybe();
        Maybe<JsonObject> postUriMaybe = postService.rxGetById(comment.getPostId())
                .map(post -> new JsonObject().put("title", post.getTitle()).put("uri", post.getUri()))
                .toMaybe();
        Maybe<List<Vote>> voteListMaybe = voteRepository.findByCommentId(comment.getId()).defaultIfEmpty(new ArrayList<>());

        return Maybe.zip(creatorSummaryMaybe, postUriMaybe, voteListMaybe, (creatorSummary, post, votes) ->
                JsonObject.mapFrom(comment)
                        .put("extra", new JsonObject()
                                .put("creatorSummary", creatorSummary)
                                .put("post", post)
                                .put("votes", votes)));
    }
}
