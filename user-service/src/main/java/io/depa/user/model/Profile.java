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
public class Profile extends DateAudit {

    private Long id;
    private Long userId;
    private String about;
    private String birthDate;
    private String fullName;
    private String gender;
    private String imageUrl;
    private String location;

    public Profile(JsonObject jsonObject) {
        ProfileConverter.fromJson(jsonObject, this);
    }

    public static Profile createInstance(Long userId) {
        Profile profile = new Profile();
        setTimestamp(profile);
        profile.setUserId(userId);
        return profile;
    }

    public static Profile createInstance(Long userId, JsonObject jsonObject) {
        Profile profile = new Profile(jsonObject);
        setTimestamp(profile);
        profile.setUserId(userId);
        return profile;
    }

    public Profile merge(Profile profile) {
        setUpdateTimestamp(this);
        if (profile.getAbout() != null) this.setAbout(profile.getAbout());
        if (profile.getBirthDate() != null) this.setBirthDate(profile.getBirthDate());
        if (profile.getFullName() != null) this.setFullName(profile.getFullName());
        if (profile.getGender() != null) this.setGender(profile.getGender());
        if (profile.getImageUrl() != null) this.setImageUrl(profile.getImageUrl());
        if (profile.getLocation() != null) this.setLocation(profile.getLocation());
        return this;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        ProfileConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
