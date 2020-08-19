package io.depa.message;

import io.depa.common.RestfulVerticle;
import io.depa.common.util.Runner;
import io.depa.message.service.MessageService;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageVerticle extends RestfulVerticle {

    private static final Integer DEFAULT_PORT = 8003;

    private MessageService messageService;

    public static void main(String[] args) {
        Runner.run(MessageVerticle.class, MessageService.NAME);
    }

    @Override
    public void start() {

        Integer port = config().getInteger("http.port", DEFAULT_PORT);
        String host = config().getString("http.host", localHost.getHostAddress());
        String startInfo = "Service <" + MessageService.NAME + "> start at port: " + port;

        messageService = MessageService.create();

        OpenAPI3RouterFactory.rxCreate(vertx, config().getString("open.api")).subscribe(factory -> {

            factory.addSecurityHandler("bearerAuth", JWTAuthHandler.create(createJWTAuth(vertx)));

            // Static operations:
            factory.addHandlerByOperationId("getStatic", StaticHandler.create());

            vertx.createHttpServer()
                    .requestHandler(factory.getRouter())
                    .rxListen(port, host)
                    .ignoreElement()
                    .andThen(publishEventBusService(MessageService.NAME, MessageService.ADDRESS, MessageService.class.getName()))
                    .andThen(publishHttpEndpoint(MessageService.NAME, host, port))
                    .andThen(registerHandler(MessageService.ADDRESS, MessageService.class, messageService))
                    .subscribe(() -> log.info(startInfo), e -> log.error(e.getMessage()));
            }, e -> log.error(e.getMessage()));
    }
}
