package io.depa.common.util;

import io.depa.common.data.audit.UserDateAudit;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.impl.CompositeFutureImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.impl.jose.JWT;
import io.vertx.reactivex.ext.web.RoutingContext;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public interface Helper {

    static <R> Future<List<R>> allOfFutures(List<Future<R>> futures) {
        return CompositeFutureImpl.all(futures.toArray(new Future[0]))
                .map(v -> futures.stream().map(Future::result).collect(Collectors.toList()));
    }

    static Single<JsonObject> getPayload(String token, JWT jwt) {
        return Single.just(token).map(jwt::decode).flatMap(payload ->
                Constants.JWT_ISSUER.equals(payload.getString("iss")) ?
                        Single.just(payload) : Single.error(new RuntimeException("Issuer is not valid")));
    }

    static <T extends UserDateAudit> List<Long> getCreatorMap(List<T> list) {
        return list.stream()
                .map(T::getCreatedBy)
                .distinct()
                .collect(Collectors.toList());
    }

    static <T> T getRandom(List<T> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    static void mapPageParams(RoutingContext context, BiConsumer<Integer, Integer> biConsumer) {
        Integer number = Integer.parseInt(context.pathParam("number"));
        Integer size = Integer.parseInt(context.pathParam("size"));
        biConsumer.accept(number, size);
    }

    static void objectNotNullHandler(Object object, Runnable runnable) {
        if (object != null) {
            runnable.run();
        }
    }
}
