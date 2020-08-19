package io.depa.post.query;

import io.depa.common.type.ActionType;

public interface PostQuery {

    String DELETE_ALL = "delete from post";

    String DELETE_BY_ID = "delete from post where id = ?";

    String INSERT = "insert into post (\n" +
            "categoryId,\n" +
            "pending,\n" +
            "body,\n" +
            "content,\n" +
            "imageUrl,\n" +
            "title,\n" +
            "uri,\n" +
            "createdAt,\n" +
            "updatedAt,\n" +
            "createdBy,\n" +
            "updatedBy)\n" +
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    String INSERT_TRACKING_POST = "insert into tracking_post (\n" +
            "actionId,\n" +
            "entityId,\n" +
            "createdAt,\n" +
            "updatedAt,\n" +
            "createdBy,\n" +
            "updatedBy)\n" +
            "values (?, ?, ?, ?, ?, ?)\n" +
            "on duplicate key update\n" +
            "updatedAt = values(updatedAt)";

    String SELECT_ALL = "select * from post";

    String SELECT_BY_BOOKMARK = "select sql_calc_found_rows post.* from post\n" +
            "inner join bookmark on bookmark.postId = post.id\n" +
            "where bookmark.userId = ?\n" +
            "order by createdAt desc limit ?, ?";

    String SELECT_BY_CATEGORY = "select sql_calc_found_rows * from post\n" +
            "where post.categoryId = ?\n" +
            "and pending = 0\n" +
            "order by createdAt desc limit ?, ?";

    String SELECT_BY_CATEGORY_AND_TARGETING = "select sql_calc_found_rows post.* from post\n" +
            "left join tracking_post as t on t.entityId = post.id and t.createdBy = ?\n" +
            "where post.categoryId = ?\n" +
            "and (t.createdBy is null or t.actionId <> " + ActionType.READ.getId() + ")\n" +
            "and post.pending = 0\n" +
            "group by post.id\n" +
            "order by post.createdAt desc limit ?, ?";

    String SELECT_BY_CATEGORY_AND_TAG = "select sql_calc_found_rows post.* from post\n" +
            "inner join tag_post on tag_post.postId = post.id\n" +
            "where post.categoryId = ?\n" +
            "and tag_post.tagId = ?\n" +
            "and pending = 0\n" +
            "order by createdAt desc limit ?, ?";

    String SELECT_BY_ID = "select * from post where id = ?";

    String SELECT_BY_MATCH = "select sql_calc_found_rows post.* from post\n" +
            "inner join tag_post on tag_post.postId = post.id\n" +
            "inner join tag on tag_post.tagId = tag.id\n" +
            "inner join user on user.id = post.createdBy\n" +
            "where pending = 0\n" +
            "and (match (body, title) against (?)\n" +
            "or match (tag.name) against (?)\n" +
            "or match (user.username) against (?))\n" +
            "group by post.id\n" +
            "order by createdAt desc limit ?, ?";

    String SELECT_BY_NOT_BOOKMARK = "select sql_calc_found_rows post.* from post\n" +
            "left join (select * from bookmark where userId = ?) as bm on bm.postId = post.id\n" +
            "where bm.userId is null\n" +
            "order by createdAt desc limit ?, ?";

    String SELECT_BY_PENDING = "select sql_calc_found_rows * from post\n" +
            "where post.pending = ?\n" +
            "order by createdAt desc limit ?, ?";

    String SELECT_BY_TAG = "select sql_calc_found_rows post.* from post\n" +
            "inner join tag_post on tag_post.postId = post.id\n" +
            "where tag_post.tagId = ?\n" +
            "and pending = 0\n" +
            "order by createdAt desc limit ?, ?";

    String SELECT_BY_URI = "select * from post where uri = ?";

    String SELECT_BY_USER = "select sql_calc_found_rows * from post\n" +
            "where post.createdBy = ?\n" +
            "and pending = 0\n" +
            "order by createdAt desc limit ?, ?";

    String SELECT_NEIGHBOURS_BY_ID = "(select * from post\n" +
            "where id < ? and categoryId <> 4 and pending = 0 order by id desc limit 1)\n" +
            "union (select * from post\n" +
            "where id > ? and categoryId <> 4 and pending = 0 order by id asc limit 1)";

    String UPDATE_BY_ID = "update post set\n" +
            "categoryId = ?,\n" +
            "body = ?,\n" +
            "content = ?,\n" +
            "imageUrl = ?,\n" +
            "title = ?,\n" +
            "uri = ?,\n" +
            "updatedAt = ?,\n"+
            "updatedBy = ?\n" +
            "where id = ?";

    String UPDATE_ORIGIN_BY_ID = "update post set originId = ? where id = ?";

    String UPDATE_PENDING_BY_ID = "update post set pending = ? where id = ?";

    String UPDATE_VIEWS_BY_ID = "update post set views = ? where id = ?";
}
