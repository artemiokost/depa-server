package io.depa.post.query;

public interface VoteQuery {

    String DELETE_ALL = "delete from vote";

    String DELETE_BY_ID = "delete from vote where id = ?";

    String INSERT = "insert into vote (\n" +
            "commentId,\n" +
            "value,\n" +
            "createdAt,\n" +
            "updatedAt,\n" +
            "createdBy,\n" +
            "updatedBy)\n" +
            "values (?, ?, ?, ?, ?, ?)";

    String SELECT_ALL = "select * from vote";

    String SELECT_BY_ID = "select * from vote where id = ?";

    String SELECT_BY_COMMENT = "select * from vote where commentId = ?";

    String UPDATE_BY_ID = "update vote set\n" +
            "value = ?,\n" +
            "updatedAt = ?\n" +
            "updatedBy = ?\n" +
            "where id = ?";
}
