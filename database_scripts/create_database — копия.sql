create database videohosting with owner postgres;

create table public.mark_type
(
    mark_id bigint generated always as identity primary key,
    name    varchar(100) not null
);

alter table public.mark_type
    owner to postgres;

create table public."user"
(
    user_id    bigint generated always as identity primary key,
    login      varchar(255) 						  unique,
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
    is_admin   boolean                  default false not null
);

alter table public."user"
    owner to postgres;

create table public.video
(
    video_id    bigint generated always as identity primary key,
    title       varchar(255) not null,
    description text,
    author_id   bigint references public."user",
    video_path  varchar(255) not null,
    created_at  timestamp with time zone default now(),
    image_path  varchar(255) not null
);

alter table public.video
    owner to postgres;

create index idx_video_author_id
    on public.video (author_id);

create table public.class
(
    class_id   bigint generated always as identity primary key,
    class_name varchar(50) not null
);

alter table public.class
    owner to postgres;

create table public.role
(
    role_id   bigint generated always as identity primary key,
    role_name varchar(50) not null
);

alter table public.role
    owner to postgres;

create table public.playlist
(
    playlist_id bigint generated always as identity primary key,
    name        varchar(255),
    user_id     bigint references public."user",
    video_id    bigint references public.video,
    updated_at  timestamp with time zone default now()
);

alter table public.playlist
    owner to postgres;

create index idx_playlist_user_id
    on public.playlist (user_id);

create table public.comment
(
    comment_id  bigint generated always as identity primary key,
    user_id     bigint references public."user",
    video_id    bigint references public.video,
    text        text    not null,
    parent_id   bigint,
    is_modified boolean not null,
    created_at  timestamp with time zone default now()
);

alter table public.comment
    owner to postgres;

create index idx_comment_user_id
    on public.comment (user_id);

create index idx_comment_video_id
    on public.comment (video_id);

create table public.user_comment_mark
(
    user_id    bigint not null,
    comment_id bigint not null,
    mark       bigint not null references public.mark_type,
    primary key (user_id, comment_id)
);

alter table public.user_comment_mark
    owner to postgres;

create index idx_user_comment_mark_user_id
    on public.user_comment_mark (user_id);

create table public.user_video_mark
(
    user_id  bigint not null,
    video_id bigint not null,
    mark     bigint not null references public.mark_type,
    primary key (user_id, video_id)
);

alter table public.user_video_mark
    owner to postgres;

create index idx_user_video_mark_user_id
    on public.user_video_mark (user_id);

create table public.role_assignment
(
    receiver_id bigint not null references public."user",
    role_id     bigint not null references public.role,
    assigned_at timestamp with time zone default now(),
    class_id    bigint not null references public.class,
    is_fixed    boolean default false not null,
    primary key (receiver_id, class_id)
);

alter table public.role_assignment
    owner to postgres;

create index idx_role_assignment_receiver_id
    on public.role_assignment (receiver_id);

create table public.video_class
(
    video_id bigint not null references public.video,
    class_id bigint not null references public.class,
    primary key (video_id, class_id)
);

alter table public.video_class
    owner to postgres;

create index idx_video_class_video_id
    on public.video_class (video_id);

create table public.video_views
(
    view_id    bigint generated always as identity primary key,
    video_id   bigint                                 not null references public.video,
    user_id    bigint references public."user",
    viewed_at  timestamp with time zone default now() not null,
    ip_address varchar(255)                           not null
);

alter table public.video_views
    owner to postgres;

create index idx_video_views_video
    on public.video_views (video_id);

create index idx_video_views_user
    on public.video_views (user_id);

create index idx_video_views_combined
    on public.video_views (video_id, user_id);

create index idx_video_views_viewed_at
    on public.video_views (viewed_at);

create table public.subscription
(
    subscriber_id bigint not null constraint subscriptions_subscriber_id_fkey references public."user",
    author_id     bigint not null constraint subscriptions_author_id_fkey references public."user",
    subscribed_at timestamp with time zone default now(),
    constraint subscriptions_pkey primary key (subscriber_id, author_id)
);

alter table public.subscription
    owner to postgres;

create index idx_subscriptions_subscriber_id
    on public.subscription (subscriber_id);

