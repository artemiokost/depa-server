package io.depa.user.query;

public interface PermissionQuery {

    String DELETE_ALL = "delete from permission";

    String DELETE_BY_ID = "delete from permission where id = ?";

    String INSERT = "insert into permission (name) values (?)";

    String INSERT_ROLE_PERMISSION = "insert into role_permission (roleId, permissionId) values (?, ?)";

    String SELECT_ALL = "select * from permission";

    String SELECT_BY_ID = "select * from permission where id = ?";

    String SELECT_BY_NAME = "select * from permission where name = ?";

    String SELECT_BY_ROLE = "select permission.name from role\n" +
            "inner join role_permission on role_permission.permissionId = permission.id\n" +
            "inner join role on role_permission.roleId = role.id\n" +
            "where permission.id = ?";

    String SELECT_BY_USERNAME = "select permission.name from user\n" +
            "inner join role_user on role_user.userId = user.id\n" +
            "inner join role_permission on role_permission.roleId = role_user.roleId\n" +
            "inner join permission on permission.id = role_permission.permissionId\n" +
            "where user.username = ?";

    String UPDATE_BY_ID = "update permission set name = ? where id = ?";
}
