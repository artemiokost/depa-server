package io.depa.post.model;

import io.depa.common.data.audit.UserDateAudit;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
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
public class Vote extends UserDateAudit {

    private Long id;
    @GenIgnore
    private Long commentId;
    private Boolean value;

    public Vote(JsonObject jsonObject) {
        VoteConverter.fromJson(jsonObject, this);
    }

    public static Vote createInstance(Boolean value, Long commentId, Long userId) {
        Vote vote = new Vote();
        setTimestamp(vote);
        vote.setCommentId(commentId);
        vote.setValue(value);
        vote.setCreatedBy(userId);
        vote.setUpdatedBy(userId);
        return vote;
    }

    public Vote merge(Vote vote) {
        setUpdateTimestamp(this);
        if (vote.getValue() != null) this.setValue(vote.getValue());
        if (vote.getUpdatedBy() != null) this.setUpdatedBy(vote.getUpdatedBy());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        VoteConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
