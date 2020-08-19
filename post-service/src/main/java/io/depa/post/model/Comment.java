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
public class Comment extends UserDateAudit {

    private Long id;
    private Long postId;
    private String content;

    public Comment(JsonObject jsonObject) {
        CommentConverter.fromJson(jsonObject, this);
    }

    public static Comment createInstance(JsonObject jsonObject, Long postId, Long userId) {
        Comment comment = new Comment(jsonObject);
        setTimestamp(comment);
        comment.setPostId(postId);
        comment.setCreatedBy(userId);
        comment.setUpdatedBy(userId);
        return comment;
    }

    public Comment merge(Comment comment) {
        setUpdateTimestamp(this);
        if (comment.getContent() != null) this.setContent(comment.getContent());
        if (comment.getUpdatedBy() != null) this.setUpdatedBy(comment.getUpdatedBy());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        CommentConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
