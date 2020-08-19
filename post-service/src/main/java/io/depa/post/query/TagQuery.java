package io.depa.post.query;

public interface TagQuery {

    String DELETE_ALL = "delete from tag";

    String DELETE_BY_ID = "delete from tag where `id` = ?";

    String INSERT = "insert into tag (name) values (?)";

    String INSERT_TAG_POST = "insert into tag_post (tagId, postId) values (?, ?)";

    String SELECT_ALL = "SELECT * FROM tag";

    String SELECT_BY_ID = "select * from tag where id = ?";

    String SELECT_BY_MATCH = "select sql_calc_found_rows tag.* from tag\n" +
            "where name like ?\n" +
            "order by name asc limit ?, ?";

    String SELECT_BY_POST_ID = "select tag.* from post\n" +
            "inner join tag_post on tag_post.postId = post.id\n" +
            "inner join tag on tag_post.tagId = tag.id\n" +
            "where post.id = ?";

    String SELECT_BY_NAME = "select * from tag where name = ?";

    String UPDATE_BY_ID = "update tag set name = ? where id = ?";
}
