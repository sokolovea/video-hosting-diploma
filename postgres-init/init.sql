comment on database postgres is 'default administrative connection database';

create sequence class_class_id_seq;

alter sequence class_class_id_seq owner to postgres;

create table mark_type
(
    mark_id bigint generated always as identity
        primary key,
    name    varchar(100) not null
);

create table multimedia_class
(
    multimedia_class_id   bigint generated always as identity
        constraint class_pkey
            primary key,
    multimedia_class_name varchar(50) not null
);

alter table multimedia_class
    owner to postgres;

alter sequence class_class_id_seq owned by multimedia_class.multimedia_class_id;

create table role
(
    role_id   bigint generated always as identity
        primary key,
    role_name varchar(50) not null
);

alter table role
    owner to postgres;

alter table mark_type
    owner to postgres;

create table "user"
(
    user_id    bigint generated always as identity
        primary key,
    login      varchar(255)
        unique,
    password   varchar(255)                           not null,
    surname    varchar(255)                           not null,
    name       varchar(255)                           not null,
    patronymic varchar(255),
    email      varchar(255)                           not null,
    telephone  varchar(255)                           not null
        constraint user_telephone_check
            check ((telephone)::text ~ '^\+\d{1,2}-\(\d{3}\)-\d{3}-\d{2}-\d{2}$'::text),
    image_path varchar(255)                           not null,
    created_at timestamp with time zone default now(),
    is_admin   boolean                  default false not null,
    is_blocked boolean                  default false not null
);

alter table "user"
    owner to postgres;

create table video
(
    video_id    bigint generated always as identity
        primary key,
    title       varchar(255)                           not null,
    description text,
    author_id   bigint
        references "user",
    video_path  varchar(255)                           not null,
    created_at  timestamp with time zone default now(),
    image_path  varchar(255)                           not null,
    is_blocked  boolean                  default false not null,
    column_name boolean                  default false not null,
    is_deleted  boolean                  default false not null
);

comment on column video.column_name is 'is_deleted';

alter table video
    owner to postgres;

create index idx_video_author_id
    on video (author_id);

create table playlist
(
    playlist_id bigint generated always as identity
        primary key,
    name        varchar(255) not null,
    user_id     bigint
        references "user",
    updated_at  timestamp with time zone default now()
);

alter table playlist
    owner to postgres;

create index idx_playlist_user_id
    on playlist (user_id);

create table comment
(
    comment_id bigint generated always as identity
        primary key,
    user_id    bigint
        references "user",
    video_id   bigint
        references video,
    text       text    not null,
    parent_id  bigint,
    is_blocked boolean not null,
    created_at timestamp with time zone default now()
);

alter table comment
    owner to postgres;

create index idx_comment_user_id
    on comment (user_id);

create index idx_comment_video_id
    on comment (video_id);

create table user_comment_mark
(
    user_id    bigint not null,
    comment_id bigint not null,
    mark       bigint not null
        references mark_type,
    primary key (user_id, comment_id)
);

alter table user_comment_mark
    owner to postgres;

create index idx_user_comment_mark_user_id
    on user_comment_mark (user_id);

create table user_video_mark
(
    user_id  bigint not null,
    video_id bigint not null,
    mark     bigint not null
        references mark_type,
    primary key (user_id, video_id)
);

alter table user_video_mark
    owner to postgres;

create index idx_user_video_mark_user_id
    on user_video_mark (user_id);

create table role_assignment
(
    receiver_id         bigint not null
        references "user",
    role_id             bigint not null
        references role,
    assigned_at         timestamp with time zone default now(),
    multimedia_class_id bigint not null
        constraint role_assignment_class_id_fkey
            references multimedia_class,
    primary key (receiver_id, multimedia_class_id)
);

alter table role_assignment
    owner to postgres;

create index idx_role_assignment_receiver_id
    on role_assignment (receiver_id);

create table video_class
(
    video_id            bigint not null
        references video,
    multimedia_class_id bigint not null
        constraint video_class_class_id_fkey
            references multimedia_class,
    primary key (video_id, multimedia_class_id)
);

alter table video_class
    owner to postgres;

create index idx_video_class_video_id
    on video_class (video_id);

create table video_views
(
    view_id    bigint generated always as identity
        primary key,
    video_id   bigint                                 not null
        references video,
    user_id    bigint
        references "user",
    viewed_at  timestamp with time zone default now() not null,
    ip_address varchar(255)                           not null
);

alter table video_views
    owner to postgres;

create index idx_video_views_video
    on video_views (video_id);

create index idx_video_views_user
    on video_views (user_id);

create index idx_video_views_combined
    on video_views (video_id, user_id);

create index idx_video_views_viewed_at
    on video_views (viewed_at);

create table subscription
(
    subscriber_id bigint not null
        constraint subscriptions_subscriber_id_fkey
            references "user",
    author_id     bigint not null
        constraint subscriptions_author_id_fkey
            references "user",
    subscribed_at timestamp with time zone default now(),
    constraint subscriptions_pkey
        primary key (subscriber_id, author_id)
);

alter table subscription
    owner to postgres;

create index idx_subscriptions_subscriber_id
    on subscription (subscriber_id);

create table playlist_video
(
    playlist_id bigint not null
        references playlist
            on delete cascade,
    video_id    bigint not null
        references video
            on delete cascade,
    primary key (playlist_id, video_id)
);

alter table playlist_video
    owner to postgres;

create index idx_playlist_video_playlist_id
    on playlist_video (playlist_id);

create index idx_playlist_video_video_id
    on playlist_video (video_id);


INSERT INTO role (role_name) VALUES 
('USER'),
('EXPERT');

INSERT INTO mark_type (name) VALUES 
('LIKE'),
('DISLIKE');

INSERT INTO "multimedia_class" (multimedia_class_name) VALUES 
('программирование'),
('электроника'),
('математика'),
('базы данных'),
('история'),
('физика'),
('без класса');

INSERT INTO "user" (login, password, surname, name, patronymic, email, telephone, image_path, created_at, is_admin, is_blocked) VALUES
('root','$2a$10$RZtz/KRuzYECD0z4jZFZoe2H0AIW14qFztML75GzoVjgx9U3KhPuO','Иванов','Иван', 'Иванович', 'admin@radiktube.ru','+7-(900)-905-12-78','.\images\logo.png', CURRENT_TIMESTAMP, true, false);