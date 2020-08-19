package io.depa.post.model;

import com.ibm.icu.text.Transliterator;
import io.depa.common.data.audit.UserDateAudit;
import io.depa.common.type.RoleType;
import io.depa.user.model.UserSummary;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.stream.Collectors;

@Accessors(chain = true)
@Data
@DataObject(generateConverter = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Post extends UserDateAudit {

    private Long id;
    private Long categoryId;
    private Long originId;
    private Boolean pending;
    private Integer views;
    @GenIgnore
    private String body;
    private String content;
    private String imageUrl;
    private String title;
    private String uri;

    public Post(JsonObject jsonObject) {
        PostConverter.fromJson(jsonObject, this);
    }

    public static String extractBody(String json) {
        JsonObject jsonObject = new JsonObject(json);
        return jsonObject.getJsonArray("blocks").stream()
                .map(JsonObject::mapFrom)
                .map(block -> block.getString("text"))
                .collect(Collectors.joining(" "));
    }

    public static Post createInstance(Long categoryId, JsonObject jsonObject, UserSummary userSummary) {
        Post post = new Post(jsonObject);
        setTimestamp(post);
        post.setPending(!userSummary.getRoles().contains(RoleType.MODERATOR.name()));
        post.setBody(extractBody(post.getContent()));
        post.setCategoryId(categoryId);
        post.setCreatedBy(userSummary.getUserId());
        post.setUpdatedBy(userSummary.getUserId());
        post.setUri(Transliterator.getInstance("Russian-Latin/BGN; Any-Latin; NFKD; Any-Lower")
                .transliterate(post.title)
                .replaceAll("[^a-zA-Z- ]", "")
                .replaceAll("[\\s]+", "-"));
        return post;
    }

    public Post merge(Post post) {
        setUpdateTimestamp(this);
        if (post.getCategoryId() != null) this.setCategoryId(post.getCategoryId());
        if (post.getOriginId() != null) this.setOriginId(post.getOriginId());
        if (post.getPending() != null) this.setPending(post.getPending());
        if (post.getViews() != null) this.setViews(post.getViews());
        if (post.getContent() != null) {
            this.setBody(extractBody(post.getContent()));
            this.setContent(post.getContent());
        }
        if (post.getImageUrl() != null) this.setImageUrl(post.getImageUrl());
        if (post.getTitle() != null) this.setTitle(post.getTitle());
        if (post.getUri() != null) this.setUri(post.getUri());
        if (post.getUpdatedBy() != null) this.setUpdatedBy(post.getUpdatedBy());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        PostConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
