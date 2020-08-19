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

create table if not exists message
(
    id        bigint(20) unsigned not null auto_increment,
    recipient bigint(20) unsigned not null,
    content   longtext            not null,
    createdAt datetime            not null,
    updatedAt datetime            not null,
    createdBy bigint(20) unsigned not null,
    updatedBy bigint(20) unsigned not null,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;