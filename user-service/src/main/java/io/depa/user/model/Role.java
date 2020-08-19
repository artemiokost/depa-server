package io.depa.user.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Accessors(chain = true)
@Data
@DataObject(generateConverter = true)
@NoArgsConstructor
public class Role implements Serializable {

    private Long id;
    private String name;

    public Role(JsonObject jsonObject) {
        RoleConverter.fromJson(jsonObject, this);
    }

    public Role(String name) {
        this.name = name;
    }

    public Role merge(Role role) {
        if (role.getName() != null) this.setName(role.getName());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        RoleConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
