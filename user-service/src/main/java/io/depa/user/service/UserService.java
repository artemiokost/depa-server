package io.depa.user.service;

import io.depa.common.context.ApplicationContext;
import io.depa.user.service.impl.UserServiceImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

@ProxyGen
@VertxGen
public interface UserService {

    String ADDRESS = "service.user";
    String NAME = "user-service";

    @GenIgnore
    static UserService create() {
        return new UserServiceImpl();
    }

    @GenIgnore
    static io.depa.user.reactivex.service.UserService createProxy() {
        io.vertx.core.Vertx delegate = ApplicationContext.getVertx().getDelegate();
        UserServiceVertxEBProxy proxy = new UserServiceVertxEBProxy(delegate, ADDRESS);
        return new io.depa.user.reactivex.service.UserService(proxy);
    }

    @GenIgnore
    static io.depa.user.reactivex.service.UserService rxCreate() {
        return new io.depa.user.reactivex.service.UserService(create());
    }

    @Fluent
    UserService checkEmail(String email, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService checkUsername(String username, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService confirmEmail(String token, Long userId, Handler<AsyncResult<Void>> handler);

    @Fluent
    UserService confirmRecovery(String token, String password, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService confirmSignUp(String token, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService createSubscription(Long publisherId, Long contextUserId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService deleteSubscriptionById(Long subscriptionId, Long contextUserId, Handler<AsyncResult<Void>> handler);

    @Fluent
    UserService getAccessToken(JsonObject authInfo, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService getById(Long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService getByUsername(String username, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService getRoleListById(Long userId, Handler<AsyncResult<List<String>>> handler);

    @Fluent
    UserService getRoleListByUsername(String username, Handler<AsyncResult<List<String>>> handler);

    @Fluent
    UserService getSubscriptionPageByPubId(Integer number, Integer size, Long pubId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService getSubscriptionPageBySubId(Integer number, Integer size, Long subId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService getUserSummaryByUserId(Long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService getUserSummaryByUsername(String username, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService updateBannedByUserId(Long userId, Boolean value, Handler<AsyncResult<Void>> handler);

    @Fluent
    UserService updateById(Long userId, JsonObject jsonObject, Long contextUserId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    UserService updateProfileById(Long profileId, JsonObject jsonObject, Long contextUserId, Handler<AsyncResult<Void>> handler);

    @Fluent
    UserService verifyEmailOnRecovery(JsonObject jsonObject, Handler<AsyncResult<Void>> handler);

    @Fluent
    UserService verifyEmailOnSignUp(JsonObject jsonObject, Handler<AsyncResult<Void>> handler);
}
