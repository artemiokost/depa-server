package io.depa.user.model;

import io.depa.common.data.audit.DateAudit;
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
public class User extends DateAudit {

    private Long id;
    private Boolean banned;
    private String email;
    private String username;
    private String password;
    private String passwordSalt;

    public User(JsonObject jsonObject) {
        UserConverter.fromJson(jsonObject, this);
    }

    public static User createInstance(JsonObject jsonObject) {
        User user = new User(jsonObject);
        setTimestamp(user);
        return user;
    }

    public User merge(User user) {
        setUpdateTimestamp(this);
        if (user.getBanned() != null) this.setBanned(user.getBanned());
        if (user.getEmail() != null) this.setEmail(user.getEmail());
        if (user.getUsername() != null) this.setUsername(user.getUsername());
        if (user.getPassword() != null) this.setPassword(user.getPassword());
        if (user.getPasswordSalt() != null) this.setPasswordSalt(user.getPasswordSalt());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        UserConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
