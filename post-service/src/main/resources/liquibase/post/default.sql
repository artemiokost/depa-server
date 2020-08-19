create table if not exists user
(
    id           bigint(20) unsigned not null auto_increment,
    banned       tinyint(1)          not null default 0,
    email        varchar(255)        not null unique,
    username     varchar(255)        not null unique,
    password     varchar(255)        not null,
    passwordSalt varchar(255)        not null,
    createdAt    datetime            not null,
    updatedAt    datetime            not null,
    fulltext username_ft (username),
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

create table if not exists category
(
    id   bigint(20) unsigned not null auto_increment,
    name varchar(255)        not null unique,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

insert ignore into category (name)
values ('ARTICLE');
insert ignore into category (name)
values ('BLOG');
insert ignore into category (name)
values ('NEWS');
insert ignore into category (name)
values ('DISCUSSION');

create table if not exists origin
(
    id   bigint(20) unsigned not null auto_increment,
    name varchar(255)        not null,
    url  varchar(255)        not null unique,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

create table if not exists post
(
    id         bigint(20) unsigned not null auto_increment,
    categoryId bigint(20) unsigned not null,
    originId   bigint(20) unsigned,
    pending    tinyint(1)          not null default 1,
    views      int(20) unsigned    not null default 0,
    body       longtext            not null,
    content    longtext            not null,
    imageUrl   varchar(255),
    title      varchar(255)        not null,
    uri        varchar(255)        not null unique,
    createdAt  datetime            not null,
    updatedAt  datetime            not null,
    createdBy  bigint(20) unsigned not null,
    updatedBy  bigint(20) unsigned not null,
    foreign key (categoryId) references category (id) on delete cascade,
    foreign key (originId) references origin (id) on delete cascade on update cascade,
    fulltext body_title_ft (body, title),
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

create table if not exists comment
(
    id        bigint(20) unsigned not null auto_increment,
    postId    bigint(20) unsigned not null,
    content   longtext            not null,
    createdAt datetime            not null,
    updatedAt datetime            not null,
    createdBy bigint(20) unsigned not null,
    updatedBy bigint(20) unsigned not null,
    foreign key (postId) references post (id) on delete cascade,
    primary key (id, postId)
) engine = innodb
  default charset = utf8mb4;

create table if not exists vote
(
    id        bigint(20) unsigned not null auto_increment,
    commentId bigint(20) unsigned not null,
    value     tinyint(1)          not null default 0,
    createdAt datetime            not null,
    updatedAt datetime            not null,
    createdBy bigint(20) unsigned not null,
    updatedBy bigint(20) unsigned not null,
    constraint comment_user_idx unique (commentId, createdBy),
    foreign key (commentId) references comment (id) on delete cascade,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

create table if not exists bookmark
(
    id        bigint(20) unsigned not null auto_increment,
    postId    bigint(20) unsigned not null,
    userId    bigint(20) unsigned not null,
    createdAt datetime            not null,
    updatedAt datetime            not null,
    constraint post_user_idx unique (postId, userId),
    foreign key (postId) references post (id) on delete cascade on update cascade,
    foreign key (userId) references user (id) on delete cascade on update cascade,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

create table if not exists clap
(
    id        bigint(20) unsigned not null auto_increment,
    postId    bigint(20) unsigned not null,
    value     int(20)             not null default 0,
    createdAt datetime            not null,
    updatedAt datetime            not null,
    createdBy bigint(20) unsigned not null,
    updatedBy bigint(20) unsigned not null,
    constraint post_user_idx unique (postId, createdBy),
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

create table if not exists clap_post
(
    clapId bigint(20) unsigned not null,
    postId bigint(20) unsigned not null,
    foreign key (clapId) references clap (id) on delete cascade,
    foreign key (postId) references post (id) on delete cascade,
    primary key (clapId, postId)
) engine = innodb
  default charset = utf8mb4;

create table if not exists tag
(
    id   bigint(20) unsigned not null auto_increment,
    name varchar(255)        not null unique,
    fulltext name_ft (name),
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

create table if not exists tag_post
(
    tagId  bigint(20) unsigned not null,
    postId bigint(20) unsigned not null,
    foreign key (tagId) references tag (id) on delete cascade,
    foreign key (postId) references post (id) on delete cascade,
    primary key (tagId, postId)
) engine = innodb
  default charset = utf8mb4;

insert ignore into category (name)
values ('Movie');
insert ignore into category (name)
values ('Opinion');
insert ignore into category (name)
values ('Other');
insert ignore into category (name)
values ('Review');
insert ignore into category (name)
values ('Science');
insert ignore into category (name)
values ('Technology');

create table if not exists action
(
    id   bigint(20) unsigned not null auto_increment,
    name varchar(255)        not null unique,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

insert ignore into action (name)
values ('CREATE');
insert ignore into action (name)
values ('READ');
insert ignore into action (name)
values ('UPDATE');
insert ignore into action (name)
values ('DELETE');

create table if not exists tracking_post
(
    id        bigint(20) unsigned not null auto_increment,
    actionId  bigint(20) unsigned not null,
    entityId  bigint(20) unsigned not null,
    createdAt datetime            not null,
    updatedAt datetime            not null,
    createdBy bigint(20) unsigned not null,
    updatedBy bigint(20) unsigned not null,
    constraint unique_tracking unique (actionId, entityId, createdBy, updatedBy),
    foreign key (actionId) references post (id) on delete cascade,
    foreign key (entityId) references post (id) on delete cascade,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;