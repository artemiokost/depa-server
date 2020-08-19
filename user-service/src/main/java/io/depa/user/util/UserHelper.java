package io.depa.user.util;

import io.depa.common.data.Page;
import io.depa.common.util.Constants;
import io.depa.common.util.Comparators;
import io.depa.user.model.Subscription;
import io.depa.user.model.UserSummary;
import io.depa.user.service.UserService;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;

public final class UserHelper {

    // Services
    private static final io.depa.user.reactivex.service.UserService userService = UserService.rxCreate();

    /**
     * This static method generates an access token
     *
     * @param jwtAuth     jwt auth provider
     * @param userSummary user summary
     * @param rememberMe  boolean flag
     * @return handler
     */
    public static String generateAccessToken(JWTAuth jwtAuth, UserSummary userSummary, Boolean rememberMe) {
        Integer expiresInMinutes = rememberMe != null && rememberMe ?
                Constants.JWT_EXPIRES_AFTER_MONTH :
                Constants.JWT_EXPIRES_AFTER_DAY;
        JWTOptions jwtOptions = new JWTOptions()
                .setAlgorithm(Constants.JWT_ALGORITHM)
                .setIssuer(Constants.JWT_ISSUER)
                .setExpiresInMinutes(expiresInMinutes);
        userSummary.getRoles().forEach(jwtOptions::addPermission);
        JsonObject principal = new JsonObject()
                .put("userId", userSummary.getUserId())
                .put("username", userSummary.getUsername());
        return jwtAuth.generateToken(principal, jwtOptions);
    }

    /**
     * This static method generates a JsonObject that represents a subscription page
     * with extra content.
     *
     * @param subscriptionPage subscription page
     * @return handler
     */
    public static Maybe<JsonObject> mapSubscriptionPageToJson(Page<Subscription> subscriptionPage) {
        return Observable.just(subscriptionPage.getList())
                .flatMapIterable(e -> e)
                .flatMapSingle(UserHelper::zipSubscriptionWithExtra)
                .toSortedList(Comparators.createdAtDesc())
                .map(JsonArray::new)
                .map(jsonArray -> JsonObject.mapFrom(subscriptionPage).put("list", jsonArray))
                .toMaybe();
    }

    public static Single<JsonObject> zipSubscriptionWithExtra(Subscription subscription) {

        Single<JsonObject> publisherSummarySingle = userService.rxGetUserSummaryByUserId(subscription.getPublisherId());
        Single<JsonObject> subscriberSummarySingle = userService.rxGetUserSummaryByUserId(subscription.getSubscriberId());

        return Single.zip(publisherSummarySingle, subscriberSummarySingle, (publisherSummary, subscriberSummary) ->
                JsonObject.mapFrom(subscription)
                        .put("extra", new JsonObject()
                                .put("publisherSummary", publisherSummary)
                                .put("subscriberSummary", subscriberSummary)));
    }
}
