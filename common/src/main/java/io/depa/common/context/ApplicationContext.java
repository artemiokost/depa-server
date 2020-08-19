package io.depa.common.context;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.reactivex.Completable;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.mysqlclient.MySQLPool;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.sqlclient.PoolOptions;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
public abstract class ApplicationContext {

    // Injections
    private static Vertx vertx;
    private static JsonObject config;
    private static CircuitBreaker circuitBreaker;
    private static ServiceDiscovery serviceDiscovery;
    // Amazon Web Services
    private static AmazonS3 amazonS3;
    // SQL Clients
    private static JDBCClient jdbcClient;
    private static MySQLPool mySQLPool;

    public static Completable create(Vertx vertx, Context context) {

        ApplicationContext.vertx = vertx;
        ApplicationContext.config = context.config();

        return ApplicationContext.initialize()
                .doOnComplete(() -> log.info("Application context initialized!"))
                .doOnError(e -> log.error("Failed to initialize application context: {}", e.getMessage()))
                .andThen(ApplicationContext.liquibase())
                .doOnComplete(() -> log.info("Liquibase initialized!"))
                .doOnError(e -> log.error("Failed to initialize liquibase: {}", e.getMessage()));
    }

    private static Completable initialize() {
        try {
            ServiceDiscoveryOptions serviceDiscoveryOptions = new ServiceDiscoveryOptions().setBackendConfiguration(config);
            serviceDiscovery = ServiceDiscovery.create(vertx, serviceDiscoveryOptions);

            if (config.getJsonObject("circuit-breaker") != null) {
                JsonObject circuitBreakerConfig = config.getJsonObject("circuit-breaker") != null ?
                        config.getJsonObject("circuit-breaker") : new JsonObject();
                String circuitBreakerName = circuitBreakerConfig.getString("name", "circuit-breaker");
                CircuitBreakerOptions circuitBreakerOptions = new CircuitBreakerOptions()
                        .setMaxFailures(circuitBreakerConfig.getInteger("max-failures", 6))
                        .setTimeout(circuitBreakerConfig.getLong("timeout", 4000L))
                        .setFallbackOnFailure(true)
                        .setResetTimeout(circuitBreakerConfig.getLong("reset-timeout", 8000L));
                circuitBreaker = CircuitBreaker.create(circuitBreakerName, vertx, circuitBreakerOptions);
            }
            if (config.getJsonObject("aws") != null) {
                JsonObject awsConfig = config.getJsonObject("aws");
                String region = awsConfig.getString("s3.region");
                String s3AccessKey = awsConfig.getString("s3.accessKey");
                String s3SecretKey = awsConfig.getString("s3.secretKey");
                BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
                AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard();
                amazonS3ClientBuilder.setCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials));
                amazonS3ClientBuilder.setRegion(region);
                amazonS3 = amazonS3ClientBuilder.build();
            }
            if (config.getJsonObject("data.source") != null) {
                JsonObject dataSource = config.getJsonObject("data.source");
                // JDBC Client initialization
                jdbcClient = JDBCClient.create(vertx, dataSource);
                // Pool initialization
                MySQLConnectOptions connectOptions = new MySQLConnectOptions(dataSource).setCachePreparedStatements(true);
                PoolOptions poolOptions = new PoolOptions().setMaxSize(4);
                mySQLPool = MySQLPool.pool(vertx, connectOptions, poolOptions);
            }
            return Completable.complete();
        } catch (Exception e) {
            return Completable.error(e);
        }
    }

    private static Completable liquibase() {
        return vertx.rxExecuteBlocking(promise -> {
            try {
                JsonObject dataSource = config.getJsonObject("data.source");
                if (dataSource != null) {
                    String url = dataSource.getString("url");
                    String user = dataSource.getString("user");
                    String password = dataSource.getString("password");

                    try (Connection connection = DriverManager.getConnection(url, user, password)) {
                        String changeLogFile = config.getJsonObject("liquibase")
                                .getString("changeLogFile");
                        Database database = DatabaseFactory.getInstance()
                                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
                        Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database);
                        liquibase.update("main");
                    }
                }
                promise.complete();
            } catch (Exception e) {
                promise.fail(e);
            }
        }).ignoreElement();
    }

    public static Vertx getVertx() {
        return vertx;
    }
    public static JsonObject getConfig() {
        return config;
    }
    public static CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
    public static ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    public static AmazonS3 getAmazonS3() {
        return amazonS3;
    }
    public static JDBCClient getJdbcClient() {
        return jdbcClient;
    }
    public static MySQLPool getMySQLPool() {
        return mySQLPool;
    }
}
