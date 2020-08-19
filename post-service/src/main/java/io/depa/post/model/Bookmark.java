package io.depa.post.model;

import io.depa.common.data.audit.DateAudit;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@DataObject(generateConverter = true)
@EqualsAndHashCode(callSuper = false)
public class Bookmark extends DateAudit {

    private Long id;
    private Long postId;
    private Long userId;

    public Bookmark() {
    }

    public Bookmark(JsonObject jsonObject) {
        BookmarkConverter.fromJson(jsonObject, this);
    }

    public static Bookmark createInstance(Long postId, Long userId) {
        Bookmark bookmark = new Bookmark();
        setTimestamp(bookmark);
        bookmark.setPostId(postId);
        bookmark.setUserId(userId);
        return bookmark;
    }

    public Bookmark merge(Bookmark bookmark) {
        setUpdateTimestamp(this);
        if (bookmark.getPostId() != null) this.setPostId(bookmark.getPostId());
        if (bookmark.getUserId() != null) this.setUserId(bookmark.getUserId());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        BookmarkConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
