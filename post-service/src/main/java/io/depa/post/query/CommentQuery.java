package io.depa.post.query;

public interface CommentQuery {

    String DELETE_BY_ID = "delete from comment where id = ?";

    String DELETE_ALL = "delete from comment";

    String INSERT = "insert into comment (\n" +
            "postId,\n" +
            "content,\n" +
            "createdAt,\n" +
            "updatedAt,\n" +
            "createdBy,\n" +
            "updatedBy)\n" +
            "values (?, ?, ?, ?, ?, ?)";

    String SELECT_ALL = "select * from comment";

    String SELECT_ALL_LIMIT = "select * from comment order by createdAt desc limit 0, ?";

    String SELECT_BY_ID = "select * from comment where id = ?";

    String SELECT_BY_POST = "select sql_calc_found_rows comment.* from comment\n" +
            "where comment.postId = ?\n" +
            "order by createdAt desc limit ?, ?";

    String SELECT_COUNT_BY_POST = "select count(id) as count from comment\n" +
            "where comment.postId = ?";

    String UPDATE_BY_ID = "update comment set\n" +
            "content = ?,\n" +
            "updatedAt = ?,\n" +
            "updatedBy = ?\n" +
            "where id = ?";
}
