package io.depa.common.exception;

public interface CustomException {

    Exception VERTX_INJECTION_FAILED = new RuntimeException("Vetx is not injected into application context");
    Exception INVALID_CONTEXT_USER = new RuntimeException("Invalid context user");
    Exception INVALID_TOKEN = new RuntimeException("Invalid token");
    Exception UNAUTHORIZED = new RuntimeException("Unauthorized");
}
