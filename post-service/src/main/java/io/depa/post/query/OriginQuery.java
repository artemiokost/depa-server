package io.depa.post.query;

public interface OriginQuery {

    String DELETE_ALL = "delete from origin";

    String DELETE_BY_ID = "delete from origin where `id` = ?";

    String INSERT = "insert into origin (name, url) values (?, ?)";

    String INSERT_OR_UPDATE = "insert into origin (name, url) values (?, ?)\n" +
            "on duplicate key update id = last_insert_id(id)";

    String SELECT_ALL = "SELECT * FROM origin";

    String SELECT_BY_ID = "select * from origin where id = ?";

    String SELECT_BY_NAME = "select * from origin where name = ?";

    String UPDATE_BY_ID = "update origin set name = ?, url = ? where id = ?";
}
