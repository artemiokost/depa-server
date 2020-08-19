package io.depa.user.service.impl;

import io.depa.common.context.ApplicationContext;
import io.depa.common.exception.CustomException;
import io.depa.common.type.RoleType;
import io.depa.common.util.AmazonS3Helper;
import io.depa.common.util.Constants;
import io.depa.common.util.Helper;
import io.depa.user.model.Profile;
import io.depa.user.model.Subscription;
import io.depa.user.model.User;
import io.depa.user.model.UserSummary;
import io.depa.user.reposirory.ProfileRepository;
import io.depa.user.reposirory.RoleRepository;
import io.depa.user.reposirory.SubscriptionRepository;
import io.depa.user.reposirory.UserRepository;
import io.depa.user.service.UserService;
import io.depa.user.util.UserHelper;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.impl.jose.JWK;
import io.vertx.ext.auth.impl.jose.JWT;
import io.vertx.ext.auth.jdbc.JDBCAuthenticationOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;
import io.vertx.reactivex.CompletableHelper;
import io.vertx.reactivex.MaybeHelper;
import io.vertx.reactivex.SingleHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jdbc.JDBCAuthentication;
import io.vertx.reactivex.ext.auth.jdbc.JDBCHashStrategy;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.mail.MailClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
public class UserServiceImpl implements UserService {
    
    private static final Integer NONCE_VERSION = 0;

    // Clients
    private final MailClient mailClient;
    // Repositories
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    // Security
    private final JDBCAuthentication jdbcAuthentication;
    private final JDBCHashStrategy jdbcHashStrategy;
    private final JWT jwt;
    private final JWTAuth jwtAuth;

    public UserServiceImpl() {
        // Injection initialization
        Vertx vertx = ApplicationContext.getVertx();
        JsonObject config = ApplicationContext.getConfig();
        JDBCClient jdbcClient = ApplicationContext.getJdbcClient();
        // Client initialization
        this.mailClient = MailClient.create(vertx, new MailConfig()
                .setPort(config.getJsonObject("aws").getInteger("smtp.port"))
                .setHostname(config.getJsonObject("aws").getString("smtp.hostname"))
                .setUsername(config.getJsonObject("aws").getString("smtp.username"))
                .setPassword(config.getJsonObject("aws").getString("smtp.password"))
                .setStarttls(StartTLSOptions.REQUIRED));
        // Repository initialization
        this.profileRepository = new ProfileRepository();
        this.roleRepository = new RoleRepository();
        this.subscriptionRepository = new SubscriptionRepository();
        this.userRepository = new UserRepository();
        // Security initialization
        jdbcHashStrategy = JDBCHashStrategy.createPBKDF2(vertx);
        jdbcHashStrategy.setNonces(new JsonArray().add(1111).add(11)); // todo: custom value required

        JDBCAuthenticationOptions jdbcAuthenticationOptions = new JDBCAuthenticationOptions();
        jdbcAuthenticationOptions.setAuthenticationQuery("select password, passwordSalt from user where username = ?");
        this.jdbcAuthentication = JDBCAuthentication.create(jdbcClient, jdbcHashStrategy, jdbcAuthenticationOptions);

        PubSecKeyOptions rs256Private = new PubSecKeyOptions()
                .setAlgorithm(Constants.JWT_ALGORITHM)
                .setBuffer(Constants.PRIVATE_KEY);

        PubSecKeyOptions rs256Public = new PubSecKeyOptions()
                .setAlgorithm(Constants.JWT_ALGORITHM)
                .setBuffer(Constants.PUBLIC_KEY);

        this.jwt = new JWT().addJWK(new JWK(rs256Private)).addJWK(new JWK(rs256Public));
        this.jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions().addPubSecKey(rs256Private));
        // Robot initialization
        robotIsHere().subscribe(() -> log.info("Robot is here!"), Throwable::printStackTrace);
    }

    @Override
    public UserService checkEmail(String email, Handler<AsyncResult<JsonObject>> handler) {
        userRepository.findByEmail(email)
                .isEmpty()
                .map(status -> new JsonObject().put("status", status))
                .subscribe(SingleHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService checkUsername(String username, Handler<AsyncResult<JsonObject>> handler) {
        userRepository.findByUsername(username)
                .isEmpty()
                .map(status -> new JsonObject().put("status", status))
                .subscribe(SingleHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService confirmEmail(String token, Long contextUserId, Handler<AsyncResult<Void>> handler) {
        Helper.getPayload(token, jwt).flatMapCompletable(payload ->
                userRepository.findById(payload.getLong("userId"))
                        .switchIfEmpty(Maybe.error(CustomException.INVALID_TOKEN))
                        .flatMapCompletable(user -> Objects.equals(contextUserId, user.getId()) ?
                                userRepository.updateById(contextUserId, new User(payload)) :
                                Completable.error(CustomException.INVALID_CONTEXT_USER)))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService confirmRecovery(String token, String password, Handler<AsyncResult<JsonObject>> handler) {
        Helper.getPayload(token, jwt)
                .flatMapMaybe(payload -> userRepository.findByEmail(payload.getString("email")))
                .flatMap(user -> {
                    String salt = jdbcHashStrategy.generateSalt();
                    String hash = jdbcHashStrategy.computeHash(password, salt, NONCE_VERSION);
                    user.setPassword(hash).setPasswordSalt(salt);
                    return userRepository.updateById(user.getId(), user).andThen(Maybe.just(user));
                })
                .flatMap(user -> {
                    Maybe<Profile> profileMaybe = profileRepository.findByUserId(user.getId());
                    Maybe<List<String>> roleListMaybe = roleRepository.findByUserId(user.getId());
                    return Maybe.zip(Maybe.just(user), profileMaybe, roleListMaybe, UserSummary::createInstance);
                })
                .map(userSummary -> {
                    String accessToken = UserHelper.generateAccessToken(jwtAuth, userSummary, false);
                    return new JsonObject()
                            .put("accessToken", accessToken)
                            .put("tokenType", Constants.JWT_TOKEN_TYPE)
                            .put("userSummary", userSummary.toJson());
                })
                .map(JsonObject::mapFrom)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService confirmSignUp(String token, Handler<AsyncResult<JsonObject>> handler) {
        Helper.getPayload(token, jwt)
                .flatMap(payload -> userRepository.findByUsername(payload.getString("username"))
                        .map(User::getId)
                        .switchIfEmpty(createNew(payload)))
                .flatMapMaybe(userId -> {
                    Maybe<User> userMaybe = userRepository.findById(userId);
                    Maybe<Profile> profileMaybe = profileRepository.findByUserId(userId);
                    Maybe<List<String>> roleListMaybe = roleRepository.findByUserId(userId);
                    return Maybe.zip(userMaybe, profileMaybe, roleListMaybe, UserSummary::createInstance);
                })
                .map(userSummary -> {
                    String accessToken = UserHelper.generateAccessToken(jwtAuth, userSummary, false);
                    return new JsonObject()
                            .put("accessToken", accessToken)
                            .put("tokenType", Constants.JWT_TOKEN_TYPE)
                            .put("userSummary", userSummary.toJson());
                })
                .map(JsonObject::mapFrom)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService createSubscription(Long publisherId, Long contextUserId,
                                          Handler<AsyncResult<JsonObject>> handler) {
        userRepository.findById(contextUserId)
                .flatMap(user -> !Objects.equals(publisherId, user.getId()) ?
                        Maybe.just(user) :
                        Maybe.error(new RuntimeException("Users cannot subscribe to themselves")))
                .map(user -> Subscription.createInstance(publisherId, user.getId()))
                .flatMapSingle(subscriptionRepository::save)
                .flatMapMaybe(subscriptionRepository::findById)
                .map(JsonObject::mapFrom)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService deleteSubscriptionById(Long subscriptionId, Long contextUserId,
                                              Handler<AsyncResult<Void>> handler) {
        Maybe<Subscription> subscriptionMaybe = subscriptionRepository.findById(subscriptionId);
        Maybe<User> userMaybe = userRepository.findById(contextUserId);
        Maybe.zip(subscriptionMaybe, userMaybe, (subscription, user) -> subscription.getSubscriberId().equals(user.getId()))
                .flatMapCompletable(hasAuthority -> hasAuthority ?
                        subscriptionRepository.deleteById(subscriptionId) :
                        Completable.error(CustomException.INVALID_CONTEXT_USER))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService getAccessToken(JsonObject authInfo, Handler<AsyncResult<JsonObject>> handler) {
        jdbcAuthentication.rxAuthenticate(authInfo)
                .map(user -> user.principal().getString("username"))
                .flatMapMaybe(username -> {
                    Maybe<User> userMaybe = userRepository.findByUsername(username);
                    Maybe<Profile> profileMaybe = profileRepository.findByUsername(username);
                    Maybe<List<String>> roleListMaybe = roleRepository.findByUsername(username);
                    return Maybe.zip(userMaybe, profileMaybe, roleListMaybe, UserSummary::createInstance);
                })
                .map(userSummary -> {
                    Boolean rememberMe = authInfo.getBoolean("rememberMe");
                    String accessToken = UserHelper.generateAccessToken(jwtAuth, userSummary, rememberMe);
                    log.info("Token successfully created");
                    return new JsonObject()
                            .put("accessToken", accessToken)
                            .put("tokenType", Constants.JWT_TOKEN_TYPE)
                            .put("userSummary", userSummary.toJson());
                })
                .doOnComplete(() -> log.info("Token successfully created"))
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService getById(Long userId, Handler<AsyncResult<JsonObject>> handler) {
        userRepository.findById(userId)
                .map(JsonObject::mapFrom)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService getByUsername(String username, Handler<AsyncResult<JsonObject>> handler) {
        userRepository.findByUsername(username)
                .map(JsonObject::mapFrom)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService getRoleListById(Long userId, Handler<AsyncResult<List<String>>> handler) {
        roleRepository.findByUserId(userId).subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService getRoleListByUsername(String username, Handler<AsyncResult<List<String>>> handler) {
        roleRepository.findByUsername(username).subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService getSubscriptionPageByPubId(Integer number, Integer size, Long pubId,
                                                  Handler<AsyncResult<JsonObject>> handler) {
        subscriptionRepository.findByPubId(number, size, pubId)
                .flatMap(UserHelper::mapSubscriptionPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService getSubscriptionPageBySubId(Integer number, Integer size, Long subId,
                                                  Handler<AsyncResult<JsonObject>> handler) {
        subscriptionRepository.findBySubId(number, size, subId)
                .flatMap(UserHelper::mapSubscriptionPageToJson)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService getUserSummaryByUserId(Long userId, Handler<AsyncResult<JsonObject>> handler) {
        Maybe<User> userMaybe = userRepository.findById(userId);
        Maybe<Profile> profileMaybe = profileRepository.findByUserId(userId);
        Maybe<List<String>> roleListMaybe = roleRepository.findByUserId(userId);
        Maybe.zip(userMaybe, profileMaybe, roleListMaybe, UserSummary::createInstance)
                .map(JsonObject::mapFrom)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService getUserSummaryByUsername(String username, Handler<AsyncResult<JsonObject>> handler) {
        Maybe<User> userMaybe = userRepository.findByUsername(username);
        Maybe<Profile> profileMaybe = profileRepository.findByUsername(username);
        Maybe<List<String>> roleListMaybe = roleRepository.findByUsername(username);
        Maybe.zip(userMaybe, profileMaybe, roleListMaybe, UserSummary::createInstance)
                .map(JsonObject::mapFrom)
                .subscribe(MaybeHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService updateBannedByUserId(Long userId, Boolean value, Handler<AsyncResult<Void>> handler) {
        userRepository.updateBannedById(userId, value).subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService updateById(Long userId, JsonObject jsonObject, Long contextUserId,
                                  Handler<AsyncResult<JsonObject>> handler) {
        User newUser = new User(jsonObject);
        if (newUser.getPassword() != null) {
            String salt = jdbcHashStrategy.generateSalt();
            String hash = jdbcHashStrategy.computeHash(newUser.getPassword(), salt, NONCE_VERSION);
            newUser.setPassword(hash).setPasswordSalt(salt);
        }
        if (newUser.getEmail() != null) {
            String message = "Please go to the following URL to update your email: https://depa.io/user/confirm/email/";
            String subject = "Depa - Email update";
            JsonObject payload = new JsonObject()
                    .put("userId", userId)
                    .put("email", newUser.getEmail());
            verifyEmail(payload, message, subject).subscribe(() -> log.info("Email has been sent"), e -> log.error(e.getMessage()));
            newUser.setEmail(null);
        }
        if (Objects.equals(contextUserId, userId)) {
            Maybe<User> userMaybe = userRepository.findById(userId);
            Maybe<Profile> profileMaybe = profileRepository.findByUserId(userId);
            Maybe<List<String>> roleListMaybe = roleRepository.findByUserId(userId);

            jdbcAuthentication.rxAuthenticate(jsonObject.getJsonObject("authInfo"))
                    .flatMapCompletable(user -> userRepository.updateById(userId, newUser))
                    .andThen(Maybe.zip(userMaybe, profileMaybe, roleListMaybe, UserSummary::createInstance))
                    .map(userSummary -> new JsonObject()
                            .put("accessToken", UserHelper.generateAccessToken(jwtAuth, userSummary, false))
                            .put("tokenType", Constants.JWT_TOKEN_TYPE)
                            .put("userSummary", userSummary.toJson()))
                    .map(JsonObject::mapFrom)
                    .subscribe(MaybeHelper.toObserver(handler));
        } else {
            Completable.error(CustomException.INVALID_CONTEXT_USER)
                    .subscribe(CompletableHelper.toObserver(handler));
        }
        return this;
    }

    @Override
    public UserService updateProfileById(Long profileId, JsonObject jsonObject, Long contextUserId,
                                         Handler<AsyncResult<Void>> handler) {
        Profile newProfile = new Profile(jsonObject);
        profileRepository.findById(contextUserId).flatMapCompletable(contextUserProfile ->
                profileRepository.findById(profileId).flatMapCompletable(profile ->
                        contextUserProfile.getId().equals(profile.getId()) ?
                                !Objects.equals(newProfile.getImageUrl(), Constants.EMPTY) ?
                                        AmazonS3Helper.uploadImage("profile/" + profile.getId(), newProfile.getImageUrl())
                                                .doOnSuccess(newProfile::setImageUrl)
                                                .ignoreElement()
                                                .andThen(profileRepository.updateById(profileId, newProfile)) :
                                        AmazonS3Helper.deleteObject(profile.getImageUrl())
                                                .andThen(profileRepository.updateById(profileId, newProfile)) :
                                Completable.error(CustomException.INVALID_CONTEXT_USER)))
                .subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService verifyEmailOnRecovery(JsonObject jsonObject, Handler<AsyncResult<Void>> handler) {
        String message = "Please go to the following URL to recover access to your account: https://depa.io/user/confirm/recovery/";
        String subject = "Depa - Recovery";
        verifyEmail(jsonObject, message, subject).subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    @Override
    public UserService verifyEmailOnSignUp(JsonObject jsonObject, Handler<AsyncResult<Void>> handler) {
        String message = "Please go to the following URL to confirm your email: https://depa.io/user/confirm/signUp/";
        String subject = "Depa - Registration confirmation";
        verifyEmail(jsonObject, message, subject).subscribe(CompletableHelper.toObserver(handler));
        return this;
    }

    private Single<Long> createNew(JsonObject payload) {
        log.info("Creating new user...");
        String salt = jdbcHashStrategy.generateSalt();
        String hash = jdbcHashStrategy.computeHash(payload.getString("password"), salt, NONCE_VERSION);
        return Single.just(User.createInstance(payload).setPassword(hash).setPasswordSalt(salt))
                .flatMap(userRepository::save)
                .flatMap(userId -> profileRepository.save(Profile.createInstance(userId))
                        .flatMapMaybe(ignored -> roleRepository.findByName(RoleType.USER.name()))
                        .flatMapSingle(role -> roleRepository.saveByUser(userId, role))
                        .map(ignored -> userId))
                .doOnSuccess(userId -> log.info("User successfully created: id" + userId));
    }

    private Completable robotInit() {
        String salt = jdbcHashStrategy.generateSalt();
        String hash = jdbcHashStrategy.computeHash(Constants.ROBOT_PASSWORD, salt, NONCE_VERSION);

        User robot = User.createInstance(new JsonObject()
                .put("email", Constants.ROBOT_EMAIL)
                .put("username", Constants.ROBOT_USERNAME)
                .put("password", hash)
                .put("passwordSalt", salt));

        return userRepository.save(robot)
                .flatMap(robotId -> roleRepository.findByName(RoleType.ADMINISTRATOR.name())
                        .flatMapSingle(role -> roleRepository.saveByUser(robotId, role))
                        .map(ignored -> robotId))
                .map(robotId -> Profile.createInstance(robotId, new JsonObject()
                        .put("about", "Cute & friendly mister robot, living on depa.io")
                        .put("birthDate", Instant.now().toString())
                        .put("fullName", "Robo")
                        .put("gender", "male")
                        .put("location", "Russia")))
                .flatMap(profileRepository::save)
                .ignoreElement();
    }

    private Completable robotIsHere() {
        return userRepository.findByUsername("robot")
                .isEmpty()
                .flatMapCompletable(isEmpty -> isEmpty ? robotInit() : Completable.complete());
    }

    private Completable verifyEmail(JsonObject jsonObject, String message, String subject) {
        String token = jwt.sign(jsonObject, new JWTOptions()
                .setAlgorithm(Constants.JWT_ALGORITHM)
                .setIssuer(Constants.JWT_ISSUER));
        MailMessage mailMessage = new MailMessage()
                .setFrom("robot@depa.io")
                .setTo(jsonObject.getString("email"))
                .setSubject(subject)
                .setText(message + token);
        return mailClient.rxSendMail(mailMessage).ignoreElement();
    }
}
