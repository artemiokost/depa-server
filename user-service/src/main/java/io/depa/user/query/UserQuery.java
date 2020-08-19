package io.depa.user.query;

public interface UserQuery {

    String DELETE_ALL = "delete from user";

    String DELETE_BY_ID = "delete from user where id = ?";

    String INSERT = "insert into user (\n" +
            "email,\n" +
            "username,\n" +
            "password,\n" +
            "passwordSalt,\n" +
            "createdAt,\n" +
            "updatedAt)\n" +
            "values (?, ?, ?, ?, ?, ?)";

    String SELECT_ALL = "select * from user";

    String SELECT_BY_ID = "select * from user where id = ?";

    String SELECT_BY_EMAIL = "select * from user where email = ?";

    String SELECT_BY_USERNAME = "select * from user where username = ?";

    String UPDATE_BANNED_BY_ID = "update user set banned = ? where id = ?";

    String UPDATE_BY_ID = "update user set\n" +
            "email = ?,\n" +
            "username = ?,\n" +
            "password = ?,\n" +
            "passwordSalt = ?,\n" +
            "updatedAt = ?\n" +
            "where id = ?";
}
