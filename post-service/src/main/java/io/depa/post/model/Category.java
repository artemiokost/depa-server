package io.depa.post.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@DataObject(generateConverter = true)
@NoArgsConstructor
public class Category {

    private Long id;
    private String name;

    public Category(String name) {
        this.name = name;
    }

    public Category(JsonObject jsonObject) {
        CategoryConverter.fromJson(jsonObject, this);
    }

    @AllArgsConstructor
    public enum Name {

        ARTICLE(1),
        BLOG(2),
        NEWS(3),
        DISCUSSION(4);

        @Getter
        final Integer value;
    }

    public Category merge(Category category) {
        if (category.getName() != null) this.setName(category.getName());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        CategoryConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
