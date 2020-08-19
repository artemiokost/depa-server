package io.depa.user.model;

import io.depa.common.data.audit.DateAudit;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
@DataObject(generateConverter = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class UserSummary extends DateAudit {

    private Long userId;
    private Long profileId;
    private Boolean banned;
    private String email;
    private String username;
    private String about;
    private String birthDate;
    private String fullName;
    private String gender;
    private String imageUrl;
    private String location;
    private List<String> roles;

    public UserSummary(JsonObject jsonObject) {
        UserSummaryConverter.fromJson(jsonObject, this);
    }

    public static UserSummary createInstance(User user, Profile profile, List<String> roles) {
        UserSummary userSummary = new UserSummary();
        userSummary.setUserId(user.getId());
        userSummary.setProfileId(user.getId());
        userSummary.setBanned(user.getBanned());
        userSummary.setEmail(user.getEmail());
        userSummary.setUsername(user.getUsername());
        userSummary.setAbout(profile.getAbout());
        userSummary.setBirthDate(profile.getBirthDate());
        userSummary.setFullName(profile.getFullName());
        userSummary.setGender(profile.getGender());
        userSummary.setImageUrl(profile.getImageUrl());
        userSummary.setLocation(profile.getLocation());
        userSummary.setCreatedAt(user.getCreatedAt());
        userSummary.setUpdatedAt(user.getUpdatedAt());
        userSummary.setRoles(roles);
        return userSummary;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        UserSummaryConverter.toJson(this, jsonObject);
        return jsonObject;
    }
}
