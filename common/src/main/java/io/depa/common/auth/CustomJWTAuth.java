package io.depa.common.auth;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

public class CustomJWTAuth extends io.vertx.reactivex.ext.auth.authentication.AuthenticationProvider {

    public CustomJWTAuth(io.vertx.ext.auth.jwt.JWTAuth delegate) {
        super(delegate);
    }

    static io.vertx.ext.auth.jwt.JWTAuth create(Vertx vertx, JWTAuthOptions config) {
        return new CustomJWTAuthProviderImpl(vertx, config);
    }

    public static io.vertx.reactivex.ext.auth.jwt.JWTAuth create(io.vertx.reactivex.core.Vertx vertx, io.vertx.ext.auth.jwt.JWTAuthOptions config) {
        return io.vertx.reactivex.ext.auth.jwt.JWTAuth.newInstance(create(vertx.getDelegate(), config));
    }
}
