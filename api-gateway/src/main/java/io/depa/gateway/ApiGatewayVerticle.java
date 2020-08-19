package io.depa.gateway;

import io.depa.common.RestfulVerticle;
import io.depa.common.util.Runner;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Promise;
import io.vertx.reactivex.core.http.*;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class ApiGatewayVerticle extends RestfulVerticle {

    private static final Integer DEFAULT_PORT = 443;
    private static final Integer INITIAL_OFFSET = 1;
    private static final String DEFAULT_NAME = "api-gateway";

    public static void main(String[] args) {
        Runner.run(ApiGatewayVerticle.class, DEFAULT_NAME);
    }

    @Override
    public void start() {

        String hostAddress = localHost.getHostAddress();

        Integer port = config().getInteger("http.port", DEFAULT_PORT);
        String host = config().getString("http.host", hostAddress);
        String policy = config().getString("policy.cors", hostAddress);

        Router router = Router.router(vertx);

        PermittedOptions inboundPermitted = new PermittedOptions();
        PermittedOptions outboundPermitted = new PermittedOptions();
        SockJSBridgeOptions bridgeOptions = new SockJSBridgeOptions()
                .addInboundPermitted(inboundPermitted)
                .addOutboundPermitted(outboundPermitted);

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        sockJSHandler.bridge(bridgeOptions);

        enableCorsSupport(policy, router);

        router.route().handler(BodyHandler.create());

        router.get("/static/*").handler(StaticHandler.create());
        router.get("/v").handler(this::apiVersion);
        router.route("/eventbus/*").handler(sockJSHandler);
        router.route("/*").handler(this::requestFilter);

        HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setUseAlpn(true)
                .setSsl(true)
                .setCompressionSupported(true)
                .setDecompressionSupported(true)
                .setPemKeyCertOptions(new PemKeyCertOptions()
                        .setCertPath(config().getString("ssl.crt"))
                        .setKeyPath(config().getString("ssl.key")));

        vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .rxListen(port, host)
                .ignoreElement()
                .andThen(publishApiGateway(host, port))
                .subscribe(() -> log.info("Service <" + DEFAULT_NAME + "> start at port: " + port), e -> log.error(e.getMessage()));
    }

    private void apiVersion(RoutingContext context) {
        context.response()
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("version", "1.0").encodePrettily());
    }

    private void requestFilter(RoutingContext context) {
        String path = context.request().uri();

        if (path.length() == INITIAL_OFFSET) {
            notFound(context);
        } else {
            circuitBreaker.rxExecute(promise ->
                    serviceDiscovery.rxGetRecords(record -> record.getType().equals(HttpEndpoint.TYPE))
                            .doOnSuccess(recordList -> dispatchRequest(context, path, recordList, promise))
                            .doOnError(error -> promise.fail(error.getCause()))
                            .subscribe())
                    .doOnError(error -> badGateway(context, error))
                    .subscribe();
        }
    }

    /**
     * Dispatch the request to the downstream REST layers.
     *
     * @param context    routing context instance
     * @param path       relative path
     * @param recordList list of HTTP client records
     */
    private void dispatchRequest(RoutingContext context, String path, List<Record> recordList,
                                 Promise<Object> promise) {

        String prefix = path.substring(INITIAL_OFFSET, path.indexOf("/", INITIAL_OFFSET));
        String newPath = path.substring(INITIAL_OFFSET + prefix.length());

        Optional<Record> optionalRecord = recordList.stream()
                .filter(record -> record.getMetadata().getString("api.name") != null)
                .filter(record -> record.getMetadata().getString("api.name").equals(prefix))
                .findAny(); // simple load balance

        HttpServerRequest serverRequest = context.request();
        HttpServerResponse serverResponse = context.response();

        if (optionalRecord.isPresent()) {
            Record record = optionalRecord.get();
            HttpClient client = serviceDiscovery.getReference(record).getAs(HttpClient.class);

            MultiMap headers = serverRequest.headers();
            HttpMethod method = serverRequest.method();

            RequestOptions requestOptions = new RequestOptions()
                    .setMethod(method)
                    .setURI(newPath)
                    .setHeaders(headers.getDelegate());

            Single<HttpClientResponse> httpClientResponseSingle = context.getBody() != null ?
                    client.rxSend(requestOptions, context.getBody()) :
                    client.rxSend(requestOptions);

            httpClientResponseSingle
                    .doOnSuccess(successHandler(promise, client, serverResponse))
                    .doOnError(promise::fail)
                    .subscribe();
        } else {
            notFound(context);
            promise.complete();
        }
    }

    private static Consumer<HttpClientResponse> successHandler(Promise<Object> promise, HttpClient client,
                                                               HttpServerResponse serverResponse) {
        return clientResponse -> clientResponse.bodyHandler(body -> {
            if (clientResponse.statusCode() >= 500) {
                promise.fail(clientResponse.statusCode() + ": " + body.toString());
            } else {
                clientResponse.headers().getDelegate().forEach(entry -> serverResponse.putHeader(entry.getKey(), entry.getValue()));
                serverResponse.setStatusCode(clientResponse.statusCode());
                serverResponse.end(body);
                promise.complete();
            }
            ServiceDiscovery.releaseServiceObject(serviceDiscovery, client);
        });
    }
}
