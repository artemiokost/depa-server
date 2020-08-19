package io.depa.message.model;

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
public class Message extends UserDateAudit {

    private Long id;
    private Integer recipient;
    private String content;

    public Message(JsonObject jsonObject) {
        MessageConverter.fromJson(jsonObject, this);
    }

    public static Message createInstance(JsonObject jsonObject) {
        Message message = new Message(jsonObject);
        setTimestamp(message);
        return message;
    }

    public Message merge(Message message) {
        setUpdateTimestamp(this);
        if (message.getRecipient() != null) this.setRecipient(message.getRecipient());
        if (message.getContent() != null) this.setContent(message.getContent());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        MessageConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
