package io.depa.post.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@DataObject(generateConverter = true)
@NoArgsConstructor
public class Tag {

    private Long id;
    private String name;

    public Tag(String name) {
        this.name = name;
    }

    public Tag(JsonObject jsonObject) {
        TagConverter.fromJson(jsonObject, this);
    }

    public Tag merge(Tag tag) {
        if (tag.getName() != null) this.setName(tag.getName());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        TagConverter.toJson(this, jsonObject);
        return jsonObject;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null || object.getClass() != getClass()) return false;
        return name.equals(((Tag) object).name);
    }

    @Override
    public int hashCode() {
        return 8 * name.length() + name.hashCode();
    }
}
