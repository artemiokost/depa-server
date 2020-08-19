package io.depa.message.service.impl;

import io.depa.message.reposirory.MessageRepository;
import io.depa.message.service.MessageService;
import io.depa.user.service.UserService;

public class MessageServiceImpl implements MessageService {

    // Services
    private final io.depa.user.reactivex.service.UserService userService;
    // Repositories
    private final MessageRepository messageRepository;

    public MessageServiceImpl() {
        // Service initialization
        this.userService = UserService.createProxy();
        // Repository initialization
        this.messageRepository = new MessageRepository();
    }
}
