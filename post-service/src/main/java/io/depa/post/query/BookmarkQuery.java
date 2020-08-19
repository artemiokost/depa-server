package io.depa.post.query;

public interface BookmarkQuery {

    String DELETE_ALL = "delete from bookmark";

    String DELETE_BY_ID = "delete from bookmark where id = ?";

    String DELETE_BY_USER = "delete from bookmark where userId = ?";

    String INSERT = "insert into bookmark (\n" +
            "postId,\n" +
            "userId,\n" +
            "createdAt,\n" +
            "updatedAt)\n" +
            "values (?, ?, ?, ?)\n" +
            "on duplicate key update\n" +
            "updatedAt = values(updatedAt)";;

    String SELECT_ALL = "select * from bookmark";

    String SELECT_BY_ID = "select * from bookmark where id = ?";

    String SELECT_BY_USER_LIMIT = "select * from bookmark where userId = ? order by createdAt desc limit 0, ?";

    String UPDATE_BY_ID = "update bookmark set\n" +
            "postId = ?,\n" +
            "userId = ?,\n" +
            "createdAt,\n" +
            "updatedAt\n" +
            "where id = ?";
}
