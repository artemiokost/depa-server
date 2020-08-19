package io.depa.common.model;

import io.depa.common.data.audit.UserDateAudit;
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
public class Tracking extends UserDateAudit {

    private Long id;
    private Long actionId;
    private Long entityId;

    public Tracking(JsonObject jsonObject) {
        TrackingConverter.fromJson(jsonObject, this);
    }

    public static Tracking createInstance(Long contextUserId, Long actionId, Long entityId) {
        Tracking tracking = new Tracking();
        setTimestamp(tracking);
        tracking.setActionId(actionId);
        tracking.setEntityId(entityId);
        tracking.setCreatedBy(contextUserId);
        tracking.setUpdatedBy(contextUserId);
        return tracking;
    }

    public Tracking merge(Tracking tracking) {
        setUpdateTimestamp(this);
        if (tracking.getActionId() != null) this.setActionId(tracking.getActionId());
        if (tracking.getEntityId() != null) this.setEntityId(tracking.getEntityId());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        TrackingConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
