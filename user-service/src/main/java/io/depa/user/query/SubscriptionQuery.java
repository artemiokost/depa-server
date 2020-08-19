package io.depa.user.query;

public interface SubscriptionQuery {

    String DELETE_ALL = "delete from subscription";

    String DELETE_BY_ID = "delete from subscription where id = ?";

    String INSERT = "insert into subscription (\n" +
            "publisherId,\n" +
            "subscriberId,\n" +
            "createdAt,\n" +
            "updatedAt)\n" +
            "values (?, ?, ?, ?)";

    String SELECT_ALL = "select * from subscription";

    String SELECT_BY_ID = "select * from subscription where id = ?";

    String SELECT_BY_PUB = "select * from subscription\n" +
            "where publisherId = ?\n" +
            "order by createdAt desc limit ?, ?";

    String SELECT_BY_SUB = "select * from subscription\n" +
            "where subscriberId = ?\n" +
            "order by createdAt desc limit ?, ?";

    String UPDATE_BY_ID = "update subscription set\n" +
            "publisherId = ?,\n" +
            "subscriberId = ?,\n" +
            "createdAt,\n" +
            "updatedAt,\n" +
            "where id = ?";
}
