package io.depa.user;

import io.depa.common.RestfulVerticle;
import io.depa.common.exception.CustomException;
import io.depa.common.type.RoleType;
import io.depa.common.util.Helper;
import io.depa.common.util.Runner;
import io.depa.user.service.UserService;
import io.vertx.core.json.Json;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserVerticle extends RestfulVerticle {

    private static final Integer DEFAULT_PORT = 8001;

    private UserService userService;

    public static void main(String[] args) {
        Runner.run(UserVerticle.class, UserService.NAME);
    }

    @Override
    public void start() {

        Integer port = config().getInteger("http.port", DEFAULT_PORT);
        String host = config().getString("http.host", localHost.getHostAddress());
        String startInfo = "Service <" + UserService.NAME + "> start at port: " + port;

        userService = UserService.create();

        OpenAPI3RouterFactory.rxCreate(vertx, config().getString("open.api")).subscribe(factory -> {

            factory.addSecurityHandler("bearerAuth", JWTAuthHandler.create(createJWTAuth(vertx)));

            // Static operations:
            factory.addHandlerByOperationId("getStatic", StaticHandler.create());
            // User operations:
            factory.addHandlerByOperationId("checkEmail", this::checkEmail);
            factory.addHandlerByOperationId("checkUsername", this::checkUsername);
            factory.addHandlerByOperationId("confirmEmail", this::confirmEmail);
            factory.addHandlerByOperationId("confirmRecovery", this::confirmRecovery);
            factory.addHandlerByOperationId("confirmSignUp", this::confirmSignUp);
            factory.addHandlerByOperationId("createSubscription", this::createSubscription);
            factory.addHandlerByOperationId("deleteSubscriptionById", this::deleteSubscriptionById);
            factory.addHandlerByOperationId("getSubscriptionPageByPubId", this::getSubscriptionPageByPubId);
            factory.addHandlerByOperationId("getSubscriptionPageBySubId", this::getSubscriptionPageBySubId);
            factory.addHandlerByOperationId("getUserSummaryByUserId", this::getUserSummaryByUserId);
            factory.addHandlerByOperationId("recovery", this::recovery);
            factory.addHandlerByOperationId("signIn", this::signIn);
            factory.addHandlerByOperationId("signUp", this::signUp);
            factory.addHandlerByOperationId("updateBannedByUserId", this::updateBannedByUserId);
            factory.addHandlerByOperationId("updateById", this::updateById);
            factory.addHandlerByOperationId("updateProfileById", this::updateProfileById);

            vertx.createHttpServer()
                    .requestHandler(factory.getRouter())
                    .rxListen(port, host)
                    .ignoreElement()
                    .andThen(publishEventBusService(UserService.NAME, UserService.ADDRESS, UserService.class.getName()))
                    .andThen(publishHttpEndpoint(UserService.NAME, host, port))
                    .andThen(registerHandler(UserService.ADDRESS, UserService.class, userService))
                    .subscribe(() -> log.info(startInfo), e -> log.error(e.getMessage()));
            }, e -> log.error(e.getMessage()));
    }

    private void checkEmail(RoutingContext context) {
        userService.checkEmail(context.getBodyAsJson().getString("email"), resultHandler(context));
    }

    private void checkUsername(RoutingContext context) {
        userService.checkUsername(context.getBodyAsJson().getString("username"), resultHandler(context));
    }

    private void confirmEmail(RoutingContext context) {
        String token = context.getBodyAsJson().getString("token");
        Long contextUserId = context.user().principal().getLong("userId");
        userService.confirmEmail(token, contextUserId, resultVoidHandler(context));
    }

    private void confirmRecovery(RoutingContext context) {
        String token = context.getBodyAsJson().getString("token");
        String password = context.getBodyAsJson().getString("password");
        userService.confirmRecovery(token, password, resultHandler(context));
    }

    private void confirmSignUp(RoutingContext context) {
        String token = context.getBodyAsJson().getString("token");
        userService.confirmSignUp(token, resultHandler(context));
    }

    private void createSubscription(RoutingContext context) {
        Long publisherId = Long.parseLong(context.pathParam("publisherId"));
        Long contextUserId = context.user().principal().getLong("userId");
        userService.createSubscription(publisherId, contextUserId, resultHandler(context));
    }

    private void deleteSubscriptionById(RoutingContext context) {
        Long subscriptionId = Long.parseLong(context.pathParam("subscriptionId"));
        Long contextUserId = context.user().principal().getLong("userId");
        userService.deleteSubscriptionById(subscriptionId, contextUserId, resultVoidHandler(context));
    }

    private void getSubscriptionPageByPubId(RoutingContext context) {
        Long pubId = Long.parseLong(context.pathParam("pubId"));
        Helper.mapPageParams(context, (number, size) ->
                userService.getSubscriptionPageByPubId(number, size, pubId, resultHandler(context)));
    }

    private void getSubscriptionPageBySubId(RoutingContext context) {
        Long subId = Long.parseLong(context.pathParam("subId"));
        Helper.mapPageParams(context, (number, size) ->
                userService.getSubscriptionPageBySubId(number, size, subId, resultHandler(context)));
    }

    private void getUserSummaryByUserId(RoutingContext context) {
        Long userId = Long.parseLong(context.pathParam("userId"));
        userService.getUserSummaryByUserId(userId, resultHandler(context));
    }

    private void recovery(RoutingContext context) {
        userService.verifyEmailOnRecovery(context.getBodyAsJson(), resultVoidHandler(context));
    }

    private void signIn(RoutingContext context) {
        userService.getAccessToken(context.getBodyAsJson(), ar -> {
            if (ar.succeeded()) {
                context.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json")
                        .end(Json.encodePrettily(ar.result()));
            } else unauthorized(context, CustomException.UNAUTHORIZED);
        });
    }

    private void signUp(RoutingContext context) {
        userService.verifyEmailOnSignUp(context.getBodyAsJson(), resultVoidHandler(context));
    }

    private void updateBannedByUserId(RoutingContext context) {
        Long userId = Long.parseLong(context.pathParam("userId"));
        Boolean value = Boolean.parseBoolean(context.pathParam("value"));
        hasRole(RoleType.ADMINISTRATOR.name(), context.user()).subscribe(() ->
                        userService.updateBannedByUserId(userId, value, resultVoidHandler(context)),
                throwable -> unauthorized(context, throwable));
    }

    private void updateById(RoutingContext context) {
        Long userId = Long.parseLong(context.pathParam("userId"));
        Long contextUserId = context.user().principal().getLong("userId");
        userService.updateById(userId, context.getBodyAsJson(), contextUserId, resultHandler(context));
    }

    private void updateProfileById(RoutingContext context) {
        Long profileId = Long.parseLong(context.pathParam("profileId"));
        Long contextUserId = context.user().principal().getLong("userId");
        userService.updateProfileById(profileId, context.getBodyAsJson(), contextUserId, resultVoidHandler(context));
    }
}
