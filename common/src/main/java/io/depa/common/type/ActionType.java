package io.depa.common.type;

public enum ActionType {

    CREATE(1L), READ(2L), UPDATE(3L), DELETE(4L);

    private final Long id;

    ActionType(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
