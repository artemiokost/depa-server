package io.depa.user.query;

public interface ProfileQuery {

    String DELETE_ALL = "delete from profile";

    String DELETE_BY_ID = "delete from profile where id = ?";

    String INSERT = "insert into profile (\n" +
            "userId,\n" +
            "about,\n" +
            "birthDate,\n" +
            "fullName,\n" +
            "gender,\n" +
            "imageUrl,\n" +
            "location,\n" +
            "createdAt,\n" +
            "updatedAt)\n" +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String SELECT_ALL = "select * from profile";

    String SELECT_BY_ID = "select * from profile where id = ?";

    String SELECT_BY_USER_ID = "select * from profile where userId = ?";

    String SELECT_BY_USERNAME = "select profile.* from profile " +
            "inner join user on profile.userId = user.id\n" +
            "where username = ?";

    String UPDATE_BY_ID = "update profile set\n" +
            "about = ?,\n" +
            "birthDate = ?,\n" +
            "fullName = ?,\n" +
            "gender = ?,\n" +
            "imageUrl = ?,\n" +
            "location = ?,\n" +
            "updatedAt = ?\n" +
            "where id = ?";
}
