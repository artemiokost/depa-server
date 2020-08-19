package io.depa.post.util;

import io.depa.common.data.Page;
import io.depa.common.util.Comparators;
import io.depa.post.model.Clap;
import io.depa.post.model.Origin;
import io.depa.post.model.Post;
import io.depa.post.model.Tag;
import io.depa.post.reposirory.*;
import io.depa.post.service.CommentService;
import io.depa.user.service.UserService;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.List;

public final class PostHelper {

    // Repositories
    private static final ClapRepository clapRepository = new ClapRepository();
    private static final OriginRepository originRepository = new OriginRepository();
    private static final TagRepository tagRepository = new TagRepository();
    // Services
    private static final io.depa.post.reactivex.service.CommentService commentService = CommentService.createProxy();
    private static final io.depa.user.reactivex.service.UserService userService = UserService.createProxy();

    /**
     * This static method generates JsonObject that represents a post page
     * with extra content.
     *
     * @param postPage post page
     * @return handler
     */
    public static Maybe<JsonObject> mapPageToJson(Page<Post> postPage) {
        return Observable.just(postPage.getList())
                .flatMapIterable(e -> e)
                .flatMapMaybe(PostHelper::zipWithExtra)
                .toSortedList(Comparators.createdAtDesc())
                .map(JsonArray::new)
                .map(jsonArray -> JsonObject.mapFrom(postPage).put("list", jsonArray))
                .toMaybe();
    }

    public static Maybe<JsonObject> zipWithExtra(Post post) {

        Maybe<Integer> commentCountMaybe = commentService.rxGetCommentCountByPostId(post.getId()).toMaybe();

        Maybe<JsonObject> creatorSummaryMaybe = userService.rxGetUserSummaryByUserId(post.getCreatedBy()).toMaybe();

        Maybe<Origin> originMaybe = post.getOriginId() != null ?
                originRepository.findById(post.getOriginId()) :
                Maybe.just(new Origin());

        Maybe<List<Tag>> tagListMaybe = tagRepository.findByPostId(post.getId())
                .defaultIfEmpty(Collections.emptyList());

        Maybe<List<Clap>> clapListMaybe = clapRepository.findByPostId(post.getId())
                .defaultIfEmpty(Collections.emptyList());

        return Maybe.zip(commentCountMaybe, creatorSummaryMaybe, originMaybe, clapListMaybe, tagListMaybe,
                (commentCount, creatorSummary, origin, claps, tags) ->
                        JsonObject.mapFrom(post)
                                .put("content", new JsonObject(post.getContent()))
                                .put("extra", new JsonObject()
                                        .put("commentCount", commentCount)
                                        .put("creatorSummary", creatorSummary)
                                        .put("origin", JsonObject.mapFrom(origin))
                                        .put("claps", claps)
                                        .put("tags", tags)));
    }
}
