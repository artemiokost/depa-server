package io.depa.user.query;

public interface RoleQuery {

    String DELETE_ALL = "delete from role";

    String DELETE_BY_ID = "delete from role where id = ?";

    String INSERT = "insert into role (name) values (?)";

    String INSERT_ROLE_USER = "insert into role_user (roleId, userId) values (?, ?)";

    String SELECT_ALL = "select * from role";

    String SELECT_BY_ID = "select * from role where id = ?";

    String SELECT_BY_NAME = "select * from role where name = ?";

    String SELECT_BY_USER = "select role.name from user\n" +
            "inner join role_user on role_user.userId = user.id\n" +
            "inner join role on role_user.roleId = role.id\n" +
            "where user.id = ?";

    String SELECT_BY_USERNAME = "select role.name from user\n" +
            "inner join role_user on role_user.userId = user.id\n" +
            "inner join role on role_user.roleId = role.id\n" +
            "where user.username = ?";

    String UPDATE_BY_ID = "update role set name = ? where id = ?";
}
