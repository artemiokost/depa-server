package io.depa.common;

import io.depa.common.auth.CustomJWTAuth;
import io.depa.common.context.ApplicationContext;
import io.depa.common.util.Constants;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.vertx.core.Promise;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.EventBusService;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.Record;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Set;

public abstract class BaseVertical extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseVertical.class);

    protected static InetAddress localHost;
    protected static CircuitBreaker circuitBreaker;
    protected static ServiceDiscovery serviceDiscovery;
    protected static Set<Record> registeredRecords = new ConcurrentHashSet<>();

    // Need to be overridden inside of implementation!
    public void start() {
    }

    @Override
    public void start(Promise<Void> promise) throws Exception {
        localHost = InetAddress.getLocalHost();
        LOGGER.info("Current member host address is: {}", localHost.getHostAddress());
        // System initialization
        ApplicationContext.create(this.vertx, this.context)
                .doOnComplete(() -> {
                    circuitBreaker = ApplicationContext.getCircuitBreaker();
                    serviceDiscovery = ApplicationContext.getServiceDiscovery();
                    this.start();
                })
                .doOnError(promise::fail)
                .subscribe();
    }

    @Override
    public void stop(Promise<Void> promise) throws Exception {
        Observable.fromIterable(registeredRecords)
                .flatMapCompletable(record -> serviceDiscovery.rxUnpublish(record.getRegistration()))
                .subscribe(CompletableHelper.toObserver(promise));
    }

    protected JWTAuth createJWTAuth(Vertx vertx) {
        return CustomJWTAuth.create(vertx, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm(Constants.JWT_ALGORITHM)
                        .setBuffer(Constants.PUBLIC_KEY))
                .setJWTOptions(new JWTOptions()
                        .setAlgorithm(Constants.JWT_ALGORITHM)
                        .setIssuer(Constants.JWT_ISSUER)));
    }

    private Completable publish(Record record) {
        String endpoint = record.toJson().getJsonObject("location").getString("endpoint");
        String prefix = endpoint.startsWith("http") ? "HTTP Service" : "Event Bus Service";
        return serviceDiscovery.rxPublish(record)
                .doOnSuccess(ar -> registeredRecords.add(record))
                .doOnSuccess(ar -> LOGGER.info(prefix + " <" + ar.getName() + "> published"))
                .ignoreElement();
    }

    protected Completable publishApiGateway(String host, Integer port) {
        Record record = HttpEndpoint.createRecord("api-gateway", true, host, port, "/", null)
                .setType("api-gateway");
        return publish(record);
    }

    protected Completable publishEventBusService(String serviceName, String serviceAddress, String serviceClass) {
        Record record = EventBusService.createRecord(serviceName, serviceAddress, serviceClass);
        return publish(record);
    }

    protected Completable publishHttpEndpoint(String serviceName, String host, Integer port) {
        Record record = HttpEndpoint.createRecord(serviceName, host, port, "/",
                new JsonObject().put("api.name", config().getString("api.name", "")));
        return publish(record);
    }

    protected <T> Completable registerHandler(String serviceAddress, Class<T> clazz, T service) {
        try {
            new ServiceBinder(vertx.getDelegate()).setAddress(serviceAddress).register(clazz, service);
            LOGGER.info("Service handler registered successfully");
            return Completable.complete();
        } catch (Exception e) {
            return Completable.error(e);
        }
    }
}
