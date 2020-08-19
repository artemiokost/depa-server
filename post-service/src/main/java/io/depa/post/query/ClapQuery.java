package io.depa.post.query;

public interface ClapQuery {

    String DELETE_ALL = "delete from clap";

    String DELETE_BY_ID = "delete from clap where id = ?";

    String INSERT = "insert into clap (\n" +
            "postId,\n" +
            "value,\n" +
            "createdAt,\n" +
            "updatedAt,\n" +
            "createdBy,\n" +
            "updatedBy)\n" +
            "values (?, ?, ?, ?, ?, ?)";

    String INSERT_CLAP_POST = "insert into clap_post (clapId, postId) values (?, ?)";

    String SELECT_ALL = "select * from clap";

    String SELECT_BY_ID = "select * from clap where id = ?";

    String SELECT_BY_POST = "select clap.* from post\n" +
            "inner join clap_post on clap_post.postId = post.id\n" +
            "inner join clap on clap_post.clapId = clap.id\n" +
            "where post.id = ?";

    String SELECT_BY_POST_AND_USER = "select * from clap where postId = ? and createdBy = ?";

    String UPDATE_BY_ID = "update clap set\n" +
            "value = ?,\n" +
            "updatedAt = ?,\n" +
            "updatedBy = ?\n" +
            "where id = ?";
}
