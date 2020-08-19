package io.depa.message.reposirory;

import io.depa.common.repository.impl.AsyncRepository;
import io.depa.message.model.Message;
import io.depa.message.query.MessageQuery;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Tuple;

import java.util.List;

public class MessageRepository extends AsyncRepository<Message> {

    public MessageRepository() {
        super(Message.class);
    }

    @Override
    public Completable deleteAll() {
        return deleteAll(MessageQuery.DELETE_ALL);
    }

    @Override
    public Completable deleteById(Long messageId) {
        Tuple arguments = Tuple.of(messageId);
        return delete(MessageQuery.DELETE_BY_ID, arguments);
    }

    @Override
    public Maybe<List<Message>> findAll() {
        return findAll(MessageQuery.SELECT_ALL);
    }

    @Override
    public Maybe<Message> findById(Long messageId) {
        Tuple arguments = Tuple.of(messageId);
        return find(MessageQuery.SELECT_BY_ID, arguments);
    }

    @Override
    public Single<Long> save(Message message) {
        Tuple arguments = Arguments.builder()
                .add(message.getRecipient())
                .add(message.getContent())
                .add(message.getCreatedAt())
                .add(message.getUpdatedAt())
                .add(message.getCreatedBy())
                .add(message.getUpdatedBy())
                .build();
        return save(MessageQuery.INSERT, arguments);
    }

    @Override
    public Completable updateById(Long messageId, Message newMessage) {
        return findById(messageId)
                .map(old -> old.merge(newMessage))
                .map(merged -> Arguments.builder()
                        .add(merged.getRecipient())
                        .add(merged.getContent())
                        .add(merged.getUpdatedAt())
                        .add(merged.getUpdatedBy())
                        .add(merged.getId())
                        .build())
                .flatMapCompletable(arguments -> update(MessageQuery.UPDATE_BY_ID, arguments));
    }
}
