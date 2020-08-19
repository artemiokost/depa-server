package io.depa.post.model;

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
public class Clap extends UserDateAudit {

    private Long id;
    private Long postId;
    private Integer value;

    public Clap(JsonObject jsonObject) {
        ClapConverter.fromJson(jsonObject, this);
    }

    public static Clap createInstance(Long postId, Long userId, Integer value) {
        Clap clap = new Clap();
        setTimestamp(clap);
        clap.setPostId(postId);
        clap.setValue(value);
        clap.setCreatedBy(userId);
        clap.setUpdatedBy(userId);
        return clap;
    }

    public Clap merge(Clap clap) {
        setUpdateTimestamp(this);
        if (clap.getValue() != null) this.setValue(clap.getValue());
        if (clap.getUpdatedBy() != null) this.setUpdatedBy(clap.getUpdatedBy());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        ClapConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
