package io.depa.message.service;

import io.depa.common.context.ApplicationContext;
import io.depa.message.service.impl.MessageServiceImpl;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;

@VertxGen
@ProxyGen
public interface MessageService {

    String ADDRESS = "service.message";
    String NAME = "message-service";

    @GenIgnore
    static MessageService create() {
        return new MessageServiceImpl();
    }

    @GenIgnore
    static io.depa.message.reactivex.service.MessageService createProxy() {
        io.vertx.core.Vertx delegate = ApplicationContext.getVertx().getDelegate();
        MessageServiceVertxEBProxy proxy = new MessageServiceVertxEBProxy(delegate, ADDRESS);
        return new io.depa.message.reactivex.service.MessageService(proxy);
    }
}
