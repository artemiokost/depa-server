package io.depa.post.query;

public interface CategoryQuery {

    String DELETE_ALL = "delete from category";

    String DELETE_BY_ID = "delete from category where id = ?";

    String INSERT = "insert into category (name) values (?)";

    String SELECT_ALL = "select * from category";

    String SELECT_BY_ID = "select * from category where id = ?";

    String UPDATE_BY_ID = "update category set name = ? where id = ?";
}
