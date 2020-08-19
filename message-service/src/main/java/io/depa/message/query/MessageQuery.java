package io.depa.message.query;

public interface MessageQuery {

    String DELETE_ALL = "delete from message";

    String DELETE_BY_ID = "delete from message where id = ?";

    String INSERT = "insert into message (" +
            "recipient,\n" +
            "content,\n" +
            "createdAt,\n" +
            "updatedAt,\n" +
            "createdBy,\n" +
            "updatedBy)\n" +
            "values (?, ?, ?, ?, ?, ?)";

    String SELECT_ALL = "select * from message";

    String SELECT_BY_ID = "select * from message where id = ?";

    String UPDATE_BY_ID = "update message set\n" +
            "recipient = ?,\n" +
            "content = ?,\n" +
            "updatedAt = ?,\n" +
            "updatedBy = ?)\n" +
            "where id = ?";
}
