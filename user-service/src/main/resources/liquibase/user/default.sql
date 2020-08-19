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

create table if not exists profile
(
    id        bigint(20) unsigned not null auto_increment,
    userId    bigint(20) unsigned not null unique,
    about     longtext,
    birthDate varchar(255),
    fullName  varchar(255),
    gender    varchar(255),
    imageUrl  varchar(255),
    location  varchar(255),
    createdAt datetime            not null,
    updatedAt datetime            not null,
    foreign key (userId) references user (id) on delete cascade,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

create table if not exists role
(
    id   bigint(20) unsigned not null auto_increment,
    name varchar(255)        not null unique,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

create table if not exists permission
(
    id   bigint(20) unsigned not null auto_increment,
    name varchar(255)        not null unique,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;

create table role_permission
(
    roleId bigint(20) unsigned not null,
    permissionId bigint(20) unsigned not null,
    foreign key (roleId) references role (id) on delete cascade,
    foreign key (permissionId) references user (id) on delete cascade,
    primary key (roleId, permissionId)
);

create table if not exists role_user
(
    roleId bigint(20) unsigned not null,
    userId bigint(20) unsigned not null,
    foreign key (roleId) references role (id) on delete cascade,
    foreign key (userId) references user (id) on delete cascade,
    primary key (roleId, userId)
) engine = innodb
  default charset = utf8mb4;

insert ignore into role (name)
values ('ADMINISTRATOR');
insert ignore into role (name)
values ('CONTRIBUTOR');
insert ignore into role (name)
values ('MODERATOR');
insert ignore into role (name)
values ('USER');

create table if not exists subscription
(
    id           bigint(20) unsigned not null auto_increment,
    publisherId  bigint(20) unsigned not null,
    subscriberId bigint(20) unsigned not null,
    createdAt    datetime            not null,
    updatedAt    datetime            not null,
    constraint unique_pub_sub unique (publisherId, subscriberId),
    foreign key (publisherId) references user (id) on delete cascade on update cascade,
    foreign key (subscriberId) references user (id) on delete cascade on update cascade,
    primary key (id)
) engine = innodb
  default charset = utf8mb4;