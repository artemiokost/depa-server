package io.depa.user.model;

import io.depa.common.data.audit.DateAudit;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@DataObject(generateConverter = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Subscription extends DateAudit {

    private Long id;
    private Long publisherId;
    private Long subscriberId;

    public Subscription(JsonObject jsonObject) {
        SubscriptionConverter.fromJson(jsonObject, this);
    }

    public static Subscription createInstance(Long publisherId, Long subscriberId) {
        Subscription subscription = new Subscription();
        setTimestamp(subscription);
        subscription.setPublisherId(publisherId);
        subscription.setSubscriberId(subscriberId);
        return subscription;
    }

    public Subscription merge(Subscription subscription) {
        setUpdateTimestamp(this);
        if (subscription.getPublisherId() != null) this.setPublisherId(subscription.getPublisherId());
        if (subscription.getSubscriberId() != null) this.setSubscriberId(subscription.getSubscriberId());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        SubscriptionConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
