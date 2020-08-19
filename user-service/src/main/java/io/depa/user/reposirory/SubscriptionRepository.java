package io.depa.user.reposirory;

import io.depa.common.data.Page;
import io.depa.common.repository.impl.AsyncRepository;
import io.depa.user.model.Subscription;
import io.depa.user.query.SubscriptionQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class SubscriptionRepository extends AsyncRepository<Subscription> {

    public SubscriptionRepository() {
        super(Subscription.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(SubscriptionQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long subscriptionId) {
        Tuple arguments = Tuple.of(subscriptionId);
        return delete(SubscriptionQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Subscription>> findAll() {
        return findAll(SubscriptionQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Subscription> findById(Long subscriptionId) {
        Tuple arguments = Tuple.of(subscriptionId);
        return find(SubscriptionQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Subscription subscription) {
        Tuple arguments = Arguments.builder()
                .add(subscription.getPublisherId())
                .add(subscription.getSubscriberId())
                .add(subscription.getCreatedAt())
                .add(subscription.getUpdatedAt())
                .build();
        return save(SubscriptionQuery.INSERT, arguments);
    }

    @Override
    public Completable updateById(Long subscriptionId, Subscription newSubscription) {
        return findById(subscriptionId)
                .map(old -> old.merge(newSubscription))
                .map(merged -> Arguments.builder()
                        .add(merged.getPublisherId())
                        .add(merged.getSubscriberId())
                        .add(merged.getUpdatedAt())
                        .add(merged.getId())
                        .build())
                .flatMapCompletable(arguments -> update(SubscriptionQuery.UPDATE_BY_ID, arguments));
    }

    public Maybe<Page<Subscription>> findByPubId(Integer number, Integer size, Long pubId) {
        Tuple arguments = Arguments.builder()
                .add(pubId)
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(SubscriptionQuery.SELECT_BY_PUB, arguments);
    }

    public Maybe<Page<Subscription>> findBySubId(Integer number, Integer size, Long subId) {
        Tuple arguments = Arguments.builder()
                .add(subId)
                .add(calcPage(number, size))
                .add(size)
                .build();
        return findPage(SubscriptionQuery.SELECT_BY_SUB, arguments);
    }
}
