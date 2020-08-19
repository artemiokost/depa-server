package io.depa.common.util;

import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.function.Function;

public interface Comparators {

    static Comparator<JsonObject> createdAtAsc() {
        Function<JsonObject, LocalDateTime> keyExtractor = c ->
                LocalDateTime.parse(c.getString("createdAt"), Constants.DATE_TIME_FORMATTER);
        return Comparator.comparing(keyExtractor);
    }

    static Comparator<JsonObject> createdAtDesc() {
        Function<JsonObject, LocalDateTime> keyExtractor = c ->
                LocalDateTime.parse(c.getString("createdAt"), Constants.DATE_TIME_FORMATTER);
        return (c1, c2) -> keyExtractor.apply(c2).compareTo(keyExtractor.apply(c1));
    }
}
