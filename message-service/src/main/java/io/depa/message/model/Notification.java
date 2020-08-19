package io.depa.message.model;

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
public class Notification extends Message {

    private Long id;
    private Integer importance;

    public Notification(JsonObject jsonObject) {
        NotificationConverter.fromJson(jsonObject, this);
    }

    public static Notification createInstance(JsonObject jsonObject) {
        Notification notification = new Notification(jsonObject);
        setTimestamp(notification);
        return notification;
    }

    public Notification merge(Notification notification) {
        setUpdateTimestamp(this);
        if (notification.getImportance() != null) this.setImportance(notification.getImportance());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        NotificationConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
