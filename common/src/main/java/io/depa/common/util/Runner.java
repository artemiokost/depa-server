package io.depa.common.util;

import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Runner {

    Logger LOGGER = LoggerFactory.getLogger(Runner.class);

    String CONFIG = "/src/config/local.json";

    static void run(Class clazz, String serviceName) {

        final ClusterManager clusterManager = new HazelcastClusterManager();
        final VertxOptions vertxOptions = new VertxOptions().setClusterManager(clusterManager);
        final ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", serviceName + CONFIG));
        final ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions().addStore(fileStore);

        Vertx.rxClusteredVertx(vertxOptions).doOnSuccess(cluster ->
                ConfigRetriever.create(cluster, configRetrieverOptions).rxGetConfig().doOnSuccess(config ->
                        cluster.rxDeployVerticle(clazz.getName(), new DeploymentOptions().setConfig(config))
                                .subscribe(result -> LOGGER.info("Deployment id is: " + result),
                                        e -> LOGGER.error("Deployment is failed: " + e)))
                        .subscribe(result -> LOGGER.info("Configuration succeeded"),
                                e -> LOGGER.error("Configuration failed: " + e)))
                .subscribe(result -> LOGGER.info("Cluster up succeeded"),
                        e -> LOGGER.error("Cluster up failed: " + e));
    }
}
