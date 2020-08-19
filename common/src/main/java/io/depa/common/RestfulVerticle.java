package io.depa.common;

import io.reactivex.Completable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.auth.authorization.Authorizations;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;

import java.time.Instant;

/**
 * An abstract base reactive verticle that provides several methods for RESTful API.
 *
 * @author Artem Kostritsa
 */
public abstract class RestfulVerticle extends BaseVertical {

    private static String getChunk(RoutingContext context, String message, int code) {
        return new JsonObject()
                .put("message", message)
                .put("path", context.normalizedPath())
                .put("status", code)
                .put("timestamp", Instant.now())
                .encodePrettily();
    }

    /**
     * Enable CORS support.
     *
     * @param policy CORS policy
     * @param router router instance
     */
    protected void enableCorsSupport(String policy, Router router) {
        router.route().handler(CorsHandler.create(policy)
                .allowCredentials(true)
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Authorization")
                .allowedHeader("Content-Type")
                .allowedHeader("accept")
                .allowedHeader("origin")
                .allowedHeader("x-requested-with")
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.PATCH)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT));
    }

    /**
     * Enable local session storage in requests.
     *
     * @param router router instance
     */
    protected void enableLocalSession(Router router) {
        router.route().handler(SessionHandler.create(
                LocalSessionStore.create(vertx, "depa.user.session")));
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * The result requires non-empty. If empty, return <em>404 Not Found</em> status.
     *
     * @param context routing context instance
     * @return handler
     */
    protected <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context) {
        return ar -> {
            if (ar.succeeded()) {
                if (ar.result() == null) {
                    notFound(context);
                } else {
                    context.response()
                            .setStatusCode(200)
                            .putHeader("content-type", "application/json")
                            .end(Json.encodePrettily(ar.result()));
                }
            } else internalError(context, ar.cause());
        };
    }

    /**
     * This method generates handler for async methods in REST APIs.
     * The result is not needed. Only the state of the async result is required.
     *
     * @param context routing context instance
     * @return handler
     */
    protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext context) {
        return ar -> {
            if (ar.succeeded()) {
                context.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json")
                        .end(getChunk(context, "success", 200));
            } else internalError(context, ar.cause());
        };
    }

    /**
     * Checks if user has such role.
     */
    protected Completable hasRole(String role, User contextUser) {
        return contextUser.rxIsAuthorized(role).flatMapCompletable(isAuthorized -> isAuthorized ?
                Completable.complete() :
                Completable.error(new RuntimeException("Missing required role: " + role)));
    }

    protected void badGateway(RoutingContext context, Throwable cause) {
        context.response()
                .setStatusCode(502)
                .putHeader("content-type", "application/json")
                .end(getChunk(context, cause.getMessage(), 502));
    }

    protected void badRequest(RoutingContext context, Throwable cause) {
        context.response().setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(getChunk(context, cause.getMessage(), 400));
    }

    protected void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(getChunk(context, "not_found", 404));
    }

    protected void internalError(RoutingContext context, Throwable cause) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(getChunk(context, cause.getMessage(), 500));
    }

    protected void unauthorized(RoutingContext context, Throwable cause) {
        context.response().setStatusCode(401)
                .putHeader("content-type", "application/json")
                .end(getChunk(context, cause.getMessage(), 401));
    }
}
