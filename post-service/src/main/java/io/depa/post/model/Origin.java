package io.depa.post.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@DataObject(generateConverter = true)
@NoArgsConstructor
public class Origin {

    private Long id;
    private String name;
    private String url;

    public Origin(String name) {
        this.name = name;
    }

    public Origin(JsonObject jsonObject) {
        OriginConverter.fromJson(jsonObject, this);
    }

    public Origin merge(Origin origin) {
        if (origin.getName() != null) this.setName(origin.getName());
        if (origin.getUrl() != null) this.setUrl(origin.getUrl());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        OriginConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
