package io.depa.common.type;

public enum CategoryType {

    ARTICLE(1L), BLOG(2L), NEWS(3L), DISCUSSION(4L);

    private final Long id;

    CategoryType(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
